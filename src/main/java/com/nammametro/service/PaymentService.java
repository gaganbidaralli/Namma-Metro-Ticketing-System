package com.nammametro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nammametro.dto.PaymentRequestDTO;
import com.nammametro.dto.PaymentResponseDTO;
import com.nammametro.dto.TicketResponseDTO;
import com.nammametro.event.PaymentCompletedEvent;
import com.nammametro.model.*;
import com.nammametro.repository.IdempotencyRecordRepository;
import com.nammametro.repository.PaymentTransactionRepository;
import com.nammametro.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentTransactionRepository paymentRepository;
    private final IdempotencyRecordRepository idempotencyRepository;
    private final TicketRepository ticketRepository;
    private final RedisCacheService redisCacheService;
    private final KafkaEventProducer kafkaEventProducer;
    private final ObjectMapper objectMapper;

    public PaymentService(PaymentTransactionRepository paymentRepository,
                          IdempotencyRecordRepository idempotencyRepository,
                          TicketRepository ticketRepository,
                          RedisCacheService redisCacheService,
                          KafkaEventProducer kafkaEventProducer,
                          ObjectMapper objectMapper) {
        this.paymentRepository = paymentRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.ticketRepository = ticketRepository;
        this.redisCacheService = redisCacheService;
        this.kafkaEventProducer = kafkaEventProducer;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        long startTime = System.currentTimeMillis();
        String idempotencyKey = request.getIdempotencyKey();

        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency-Key header or property is mandatory for payment");
        }

        // --- STEP 1: Fast Redis Idempotency Check & Lock ---
        boolean lockAcquired = redisCacheService.acquireIdempotencyLock(idempotencyKey);
        if (!lockAcquired) {
            log.warn("Duplicate concurrent payment request detected for idempotency key: {}", idempotencyKey);
            // Check if already finalized in DB
            var existingRecord = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
            if (existingRecord.isPresent()) {
                return parseCachedPaymentResponse(existingRecord.get(), startTime);
            }
            throw new IllegalStateException("A payment with idempotency key [" + idempotencyKey + "] is currently being processed. Please wait.");
        }

        try {
            // --- STEP 2: MySQL Database Idempotency Check ---
            var existingRecord = idempotencyRepository.findByIdempotencyKey(idempotencyKey);
            if (existingRecord.isPresent()) {
                log.info("Idempotency key {} already processed. Returning cached response.", idempotencyKey);
                return parseCachedPaymentResponse(existingRecord.get(), startTime);
            }

            // --- STEP 3: Retrieve Ticket ---
            Ticket ticket = ticketRepository.findByTicketNumber(request.getTicketNumber())
                    .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + request.getTicketNumber()));

            if (ticket.getStatus() == TicketStatus.ACTIVE) {
                log.info("Ticket {} is already active.", ticket.getTicketNumber());
                // Look up transaction
                var existingTx = paymentRepository.findByTicketNumber(ticket.getTicketNumber());
                if (existingTx.isPresent()) {
                    PaymentResponseDTO res = buildResponseFromTransaction(existingTx.get(), ticket);
                    res.setDuplicateIgnored(true);
                    res.setMessage("Ticket already paid and active.");
                    res.setLatencyMs(System.currentTimeMillis() - startTime);
                    return res;
                }
            }

            if (ticket.getStatus() != TicketStatus.PAYMENT_PENDING && ticket.getStatus() != TicketStatus.CREATED) {
                throw new IllegalStateException("Ticket cannot be paid in status: " + ticket.getStatus());
            }

            // --- STEP 4: Process Payment Transaction ---
            String txId = "TXN_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
            String gatewayRef = "GW_NMM_" + System.currentTimeMillis();

            PaymentTransaction tx = new PaymentTransaction();
            tx.setTransactionId(txId);
            tx.setIdempotencyKey(idempotencyKey);
            tx.setTicketId(ticket.getId());
            tx.setTicketNumber(ticket.getTicketNumber());
            tx.setAmount(ticket.getFinalAmount());
            tx.setPaymentMethod(request.getPaymentMethod());
            tx.setStatus(PaymentStatus.SUCCESS);
            tx.setGatewayReference(gatewayRef);
            tx.setCreatedAt(LocalDateTime.now());

            paymentRepository.save(tx);

            // Transition ticket
            ticket.setStatus(TicketStatus.ACTIVE);
            ticketRepository.save(ticket);

            // --- STEP 5: Emit Kafka Event ---
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                    tx.getTransactionId(),
                    idempotencyKey,
                    ticket.getId(),
                    ticket.getTicketNumber(),
                    tx.getAmount(),
                    tx.getPaymentMethod(),
                    tx.getStatus(),
                    tx.getGatewayReference()
            );
            kafkaEventProducer.sendPaymentCompletedEvent(event);

            // Update Redis Cache with the active ticket
            TicketResponseDTO ticketDTO = new TicketResponseDTO(ticket);
            redisCacheService.cacheTicket(ticket.getTicketNumber(), ticketDTO);

            // --- STEP 6: Construct Response and Store in Idempotency Record ---
            PaymentResponseDTO response = new PaymentResponseDTO();
            response.setTransactionId(tx.getTransactionId());
            response.setIdempotencyKey(idempotencyKey);
            response.setTicketNumber(ticket.getTicketNumber());
            response.setAmount(tx.getAmount());
            response.setPaymentMethod(tx.getPaymentMethod());
            response.setStatus(tx.getStatus());
            response.setGatewayReference(tx.getGatewayReference());
            response.setDuplicateIgnored(false);
            response.setMessage("Payment successful. Ticket activated.");
            response.setIssuedTicket(ticketDTO);
            response.setTimestamp(tx.getCreatedAt());
            response.setLatencyMs(System.currentTimeMillis() - startTime);

            // Save idempotency record to MySQL for permanent deduplication
            try {
                String jsonResponse = objectMapper.writeValueAsString(response);
                IdempotencyRecord record = new IdempotencyRecord(idempotencyKey, "PAYMENT", null, jsonResponse, 200);
                idempotencyRepository.save(record);
            } catch (Exception e) {
                log.error("Failed to serialize idempotency response: {}", e.getMessage());
            }

            return response;

        } finally {
            // Note: Keep lock in Redis for the TTL duration to guard against immediate burst retries
        }
    }

    private PaymentResponseDTO parseCachedPaymentResponse(IdempotencyRecord record, long startTime) {
        try {
            PaymentResponseDTO cached = objectMapper.readValue(record.getResponseBody(), PaymentResponseDTO.class);
            cached.setDuplicateIgnored(true);
            cached.setMessage("Duplicate request suppressed by Idempotency Key. Returning cached result.");
            cached.setLatencyMs(System.currentTimeMillis() - startTime);
            return cached;
        } catch (Exception e) {
            log.error("Failed to parse cached response: {}", e.getMessage());
            throw new RuntimeException("Error reading cached idempotency record");
        }
    }

    private PaymentResponseDTO buildResponseFromTransaction(PaymentTransaction tx, Ticket ticket) {
        PaymentResponseDTO res = new PaymentResponseDTO();
        res.setTransactionId(tx.getTransactionId());
        res.setIdempotencyKey(tx.getIdempotencyKey());
        res.setTicketNumber(tx.getTicketNumber());
        res.setAmount(tx.getAmount());
        res.setPaymentMethod(tx.getPaymentMethod());
        res.setStatus(tx.getStatus());
        res.setGatewayReference(tx.getGatewayReference());
        res.setIssuedTicket(new TicketResponseDTO(ticket));
        res.setTimestamp(tx.getCreatedAt());
        return res;
    }
}
