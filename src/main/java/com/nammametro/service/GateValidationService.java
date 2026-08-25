package com.nammametro.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nammametro.dto.GateScanRequestDTO;
import com.nammametro.dto.GateScanResponseDTO;
import com.nammametro.dto.TicketResponseDTO;
import com.nammametro.event.GateEntryExitEvent;
import com.nammametro.model.*;
import com.nammametro.repository.GateLogRepository;
import com.nammametro.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class GateValidationService {

    private static final Logger log = LoggerFactory.getLogger(GateValidationService.class);

    private final TicketRepository ticketRepository;
    private final GateLogRepository gateLogRepository;
    private final StationService stationService;
    private final RedisCacheService redisCacheService;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${nammametro.fare.max-stay-minutes:120}")
    private long maxStayMinutes = 120;

    @Value("${nammametro.fare.penalty-per-extra-hour:20.0}")
    private double penaltyPerExtraHour = 20.0;

    public GateValidationService(TicketRepository ticketRepository,
                                 GateLogRepository gateLogRepository,
                                 StationService stationService,
                                 RedisCacheService redisCacheService,
                                 KafkaEventProducer kafkaEventProducer,
                                 ObjectMapper objectMapper) {
        this.ticketRepository = ticketRepository;
        this.gateLogRepository = gateLogRepository;
        this.stationService = stationService;
        this.redisCacheService = redisCacheService;
        this.kafkaEventProducer = kafkaEventProducer;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public GateScanResponseDTO processGateScan(GateScanRequestDTO request) {
        long startTime = System.currentTimeMillis();

        Station currentStation = stationService.findStationByCodeOrId(request.getStationCode())
                .orElseThrow(() -> new IllegalArgumentException("Station not found: " + request.getStationCode()));

        String ticketNumber = extractTicketNumber(request.getScanPayload());

        GateScanResponseDTO response = new GateScanResponseDTO();
        response.setTurnstileId(request.getTurnstileId());
        response.setStationName(currentStation.getNameEn());
        response.setGateType(request.getGateType());
        response.setTicketNumber(ticketNumber);
        response.setTimestamp(LocalDateTime.now());
        response.setPenaltyCharged(0.0);

        // Fetch Ticket
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber).orElse(null);
        if (ticket == null) {
            return rejectGateScan(request, currentStation, ticketNumber, "Invalid QR Code / Ticket not found", startTime);
        }

        LocalDateTime now = LocalDateTime.now();

        // Validate Expiry
        if (now.isAfter(ticket.getValidUntil())) {
            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);
            redisCacheService.evictTicket(ticketNumber);
            return rejectGateScan(request, currentStation, ticketNumber, "Ticket Expired at " + ticket.getValidUntil(), startTime);
        }

        if (request.getGateType() == GateType.ENTRY) {
            // --- TAP-IN (ENTRY) VALIDATION ---
            if (ticket.getStatus() == TicketStatus.PAYMENT_PENDING || ticket.getStatus() == TicketStatus.CREATED) {
                return rejectGateScan(request, currentStation, ticketNumber, "Payment Pending. Please complete payment before entry.", startTime);
            }

            if (ticket.getStatus() == TicketStatus.IN_TRANSIT) {
                return rejectGateScan(request, currentStation, ticketNumber, "Ticket already tapped-in. Double entry prevented (Passback violation).", startTime);
            }

            if (ticket.getStatus() == TicketStatus.COMPLETED) {
                return rejectGateScan(request, currentStation, ticketNumber, "Ticket journey already completed.", startTime);
            }

            if (ticket.getStatus() != TicketStatus.ACTIVE) {
                return rejectGateScan(request, currentStation, ticketNumber, "Invalid ticket status: " + ticket.getStatus(), startTime);
            }

            // Verify origin station
            if (!ticket.getSourceStation().getId().equals(currentStation.getId())) {
                return rejectGateScan(request, currentStation, ticketNumber,
                        String.format("Origin Mismatch: Ticket valid from [%s], scanned at [%s]",
                                ticket.getSourceStation().getNameEn(), currentStation.getNameEn()), startTime);
            }

            // Successful Tap-In
            ticket.setStatus(TicketStatus.IN_TRANSIT);
            ticket.setEntryTime(now);
            ticketRepository.save(ticket);

            // Update cache
            TicketResponseDTO dto = new TicketResponseDTO(ticket);
            redisCacheService.cacheTicket(ticketNumber, dto);

            // Log Gate Entry
            GateLog logEntry = new GateLog(ticket.getId(), ticketNumber, currentStation.getId(),
                    currentStation.getNameEn(), GateType.ENTRY, request.getTurnstileId(), true, "Entry Authorized", 0.0);
            gateLogRepository.save(logEntry);

            // Publish Kafka Event
            kafkaEventProducer.sendGateEvent(new GateEntryExitEvent(
                    ticket.getId(), ticketNumber, currentStation.getId(), currentStation.getNameEn(),
                    GateType.ENTRY, request.getTurnstileId(), true, 0.0
            ));

            response.setGateOpened(true);
            response.setLedColor("GREEN");
            response.setUpdatedStatus(TicketStatus.IN_TRANSIT);
            response.setMessage(String.format("Gate Open: Welcome to Namma Metro! Entry recorded at %s.", currentStation.getNameEn()));
            response.setValidationLatencyMs(System.currentTimeMillis() - startTime);
            return response;

        } else {
            // --- TAP-OUT (EXIT) VALIDATION ---
            if (ticket.getStatus() != TicketStatus.IN_TRANSIT) {
                return rejectGateScan(request, currentStation, ticketNumber,
                        "Exit Denied: No corresponding entry tap-in found for this ticket (Status: " + ticket.getStatus() + ").", startTime);
            }

            double penalty = 0.0;
            StringBuilder message = new StringBuilder();

            // Check destination match / over-travel
            boolean destMatches = ticket.getDestinationStation().getId().equals(currentStation.getId());
            if (!destMatches) {
                log.info("Passenger exited at different station: Expected={}, Actual={}",
                        ticket.getDestinationStation().getNameEn(), currentStation.getNameEn());
                message.append("Exited at different destination. ");
            }

            // Check Overstay Penalty (> 120 minutes)
            if (ticket.getEntryTime() != null) {
                long stayMinutes = Duration.between(ticket.getEntryTime(), now).toMinutes();
                if (stayMinutes > maxStayMinutes) {
                    long extraMinutes = stayMinutes - maxStayMinutes;
                    long extraHours = (long) Math.ceil((double) extraMinutes / 60.0);
                    penalty = extraHours * penaltyPerExtraHour;
                    message.append(String.format("Overstay penalty (Stayed %d mins, limit %d mins): ₹%.2f. ",
                            stayMinutes, maxStayMinutes, penalty));
                }
            }

            // Finalize Ticket
            ticket.setStatus(TicketStatus.COMPLETED);
            ticket.setExitTime(now);
            ticket.setPenaltyAmount(penalty);
            ticketRepository.save(ticket);

            // Evict from active cache
            redisCacheService.evictTicket(ticketNumber);

            // Log Gate Exit
            GateLog logExit = new GateLog(ticket.getId(), ticketNumber, currentStation.getId(),
                    currentStation.getNameEn(), GateType.EXIT, request.getTurnstileId(), true,
                    "Exit Authorized" + (penalty > 0 ? " with penalty" : ""), penalty);
            gateLogRepository.save(logExit);

            // Publish Kafka Event
            kafkaEventProducer.sendGateEvent(new GateEntryExitEvent(
                    ticket.getId(), ticketNumber, currentStation.getId(), currentStation.getNameEn(),
                    GateType.EXIT, request.getTurnstileId(), true, penalty
            ));

            response.setGateOpened(true);
            response.setLedColor("GREEN");
            response.setUpdatedStatus(TicketStatus.COMPLETED);
            response.setPenaltyCharged(penalty);
            response.setMessage(message.length() > 0 ? message.toString() :
                    String.format("Gate Open: Thank you for travelling with Namma Metro! Exit at %s.", currentStation.getNameEn()));
            response.setValidationLatencyMs(System.currentTimeMillis() - startTime);
            return response;
        }
    }

    private GateScanResponseDTO rejectGateScan(GateScanRequestDTO request, Station currentStation,
                                               String ticketNumber, String reason, long startTime) {
        log.warn("Gate scan REJECTED at station {}: {}", currentStation.getNameEn(), reason);

        // Record failed gate attempt
        GateLog failedLog = new GateLog(0L, ticketNumber != null ? ticketNumber : "UNKNOWN",
                currentStation.getId(), currentStation.getNameEn(), request.getGateType(),
                request.getTurnstileId(), false, reason, 0.0);
        gateLogRepository.save(failedLog);

        // Publish event
        kafkaEventProducer.sendGateEvent(new GateEntryExitEvent(
                0L, ticketNumber, currentStation.getId(), currentStation.getNameEn(),
                request.getGateType(), request.getTurnstileId(), false, 0.0
        ));

        GateScanResponseDTO response = new GateScanResponseDTO();
        response.setGateOpened(false);
        response.setLedColor("RED");
        response.setTurnstileId(request.getTurnstileId());
        response.setStationName(currentStation.getNameEn());
        response.setGateType(request.getGateType());
        response.setTicketNumber(ticketNumber);
        response.setMessage("GATE CLOSED: " + reason);
        response.setPenaltyCharged(0.0);
        response.setTimestamp(LocalDateTime.now());
        response.setValidationLatencyMs(System.currentTimeMillis() - startTime);
        return response;
    }

    private String extractTicketNumber(String payload) {
        if (payload == null) return "";
        payload = payload.trim();
        if (payload.startsWith("{")) {
            try {
                JsonNode node = objectMapper.readTree(payload);
                if (node.has("ticketNumber")) {
                    return node.get("ticketNumber").asText();
                }
            } catch (Exception ignored) {}
        }
        return payload;
    }
}
