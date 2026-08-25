package com.nammametro.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nammametro.dto.FareEstimateRequest;
import com.nammametro.dto.FareEstimateResponse;
import com.nammametro.dto.TicketBookingRequest;
import com.nammametro.dto.TicketResponseDTO;
import com.nammametro.event.TicketCreatedEvent;
import com.nammametro.event.TicketIssuedEvent;
import com.nammametro.model.Station;
import com.nammametro.model.Ticket;
import com.nammametro.model.TicketStatus;
import com.nammametro.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final StationService stationService;
    private final FareCalculationService fareCalculationService;
    private final RedisCacheService redisCacheService;
    private final KafkaEventProducer kafkaEventProducer;

    @Value("${nammametro.redis.ticket-ttl-seconds:14400}")
    private long ticketTtlSeconds = 14400;

    public TicketService(TicketRepository ticketRepository,
                         StationService stationService,
                         FareCalculationService fareCalculationService,
                         RedisCacheService redisCacheService,
                         KafkaEventProducer kafkaEventProducer) {
        this.ticketRepository = ticketRepository;
        this.stationService = stationService;
        this.fareCalculationService = fareCalculationService;
        this.redisCacheService = redisCacheService;
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @Transactional
    public TicketResponseDTO bookTicket(TicketBookingRequest request) {
        Station source = stationService.findStationByCodeOrId(request.getSourceCode())
                .orElseThrow(() -> new IllegalArgumentException("Source station not found: " + request.getSourceCode()));

        Station destination = stationService.findStationByCodeOrId(request.getDestinationCode())
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found: " + request.getDestinationCode()));

        if (source.getId().equals(destination.getId())) {
            throw new IllegalArgumentException("Source and destination stations cannot be identical");
        }

        // Calculate Fare
        FareEstimateRequest fareReq = new FareEstimateRequest(
                source.getStationCode(),
                destination.getStationCode(),
                request.isSmartCardUser(),
                request.getPassengerCount()
        );
        FareEstimateResponse fareEst = fareCalculationService.calculateFare(fareReq);

        // Generate Ticket Number: NMM-{SOURCE}-{DEST}-{TIMESTAMP_RAND}
        String randomSuffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String ticketNumber = String.format("NMM-%s-%s-%s", source.getStationCode(), destination.getStationCode(), randomSuffix);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validUntil = now.plusHours(4); // 4 hours validity

        // Generate Secure Cryptographic QR Data & Hash
        String qrPayload = String.format("{\"ticketNumber\":\"%s\",\"src\":\"%s\",\"dest\":\"%s\",\"validUntil\":\"%s\",\"passengers\":%d}",
                ticketNumber, source.getStationCode(), destination.getStationCode(),
                validUntil.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), request.getPassengerCount());

        String qrHash = generateSha256(qrPayload + "_NAMMA_METRO_SECRET_SALT_2026");

        Ticket ticket = new Ticket();
        ticket.setTicketNumber(ticketNumber);
        ticket.setUserId(request.getUserId());
        ticket.setSourceStation(source);
        ticket.setDestinationStation(destination);
        ticket.setFareAmount(fareEst.getBaseFare());
        ticket.setDiscountAmount(fareEst.getDiscount());
        ticket.setFinalAmount(fareEst.getTotalFare());
        ticket.setPassengerCount(request.getPassengerCount());
        ticket.setStatus(TicketStatus.PAYMENT_PENDING);
        ticket.setQrCodeData(qrPayload);
        ticket.setQrHash(qrHash);
        ticket.setCreatedAt(now);
        ticket.setValidUntil(validUntil);

        ticketRepository.save(ticket);

        // Emit TicketCreatedEvent to Kafka
        TicketCreatedEvent createdEvent = new TicketCreatedEvent(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getUserId(),
                source.getNameEn(),
                destination.getNameEn(),
                ticket.getFinalAmount(),
                ticket.getPassengerCount()
        );
        kafkaEventProducer.sendTicketCreatedEvent(createdEvent);

        TicketResponseDTO responseDTO = new TicketResponseDTO(ticket);
        responseDTO.setQrBase64Image(generateQrBase64Image(qrPayload));
        responseDTO.setMessage("Booking created. Proceed to payment.");

        // Cache in Redis
        redisCacheService.cacheTicket(ticket.getTicketNumber(), responseDTO);

        return responseDTO;
    }

    public TicketResponseDTO getTicketByNumber(String ticketNumber) {
        // Fast Redis Cache Lookup
        Optional<TicketResponseDTO> cached = redisCacheService.getCachedTicket(ticketNumber);
        if (cached.isPresent()) {
            TicketResponseDTO dto = cached.get();
            if (dto.getQrBase64Image() == null && dto.getQrCodeData() != null) {
                dto.setQrBase64Image(generateQrBase64Image(dto.getQrCodeData()));
            }
            return dto;
        }

        // Database lookup
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found with number: " + ticketNumber));

        TicketResponseDTO responseDTO = new TicketResponseDTO(ticket);
        responseDTO.setQrBase64Image(generateQrBase64Image(ticket.getQrCodeData()));
        responseDTO.setFromCache(false);

        // Populate cache for subsequent lookups
        redisCacheService.cacheTicket(ticketNumber, responseDTO);
        return responseDTO;
    }

    public List<TicketResponseDTO> getUserTickets(String userId) {
        return ticketRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(t -> {
                    TicketResponseDTO dto = new TicketResponseDTO(t);
                    dto.setQrBase64Image(generateQrBase64Image(t.getQrCodeData()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketResponseDTO activateTicketAfterPayment(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketNumber));

        ticket.setStatus(TicketStatus.ACTIVE);
        ticketRepository.save(ticket);

        TicketResponseDTO dto = new TicketResponseDTO(ticket);
        dto.setQrBase64Image(generateQrBase64Image(ticket.getQrCodeData()));
        redisCacheService.cacheTicket(ticketNumber, dto);

        // Emit Kafka TicketIssuedEvent
        TicketIssuedEvent issuedEvent = new TicketIssuedEvent(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getUserId(),
                ticket.getSourceStation().getNameEn(),
                ticket.getDestinationStation().getNameEn(),
                ticket.getFinalAmount(),
                ticket.getValidUntil()
        );
        kafkaEventProducer.sendTicketIssuedEvent(issuedEvent);

        return dto;
    }

    public String generateQrBase64Image(String text) {
        if (text == null || text.isEmpty()) return "";
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 250, 250);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("Failed to generate QR code image: {}", e.getMessage());
            return "";
        }
    }

    private String generateSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
