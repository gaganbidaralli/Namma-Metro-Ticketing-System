package com.nammametro;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nammametro.dto.PaymentRequestDTO;
import com.nammametro.dto.PaymentResponseDTO;
import com.nammametro.model.*;
import com.nammametro.repository.IdempotencyRecordRepository;
import com.nammametro.repository.PaymentTransactionRepository;
import com.nammametro.repository.TicketRepository;
import com.nammametro.service.KafkaEventProducer;
import com.nammametro.service.PaymentService;
import com.nammametro.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class IdempotentPaymentTest {

    private PaymentTransactionRepository paymentRepository;
    private IdempotencyRecordRepository idempotencyRepository;
    private TicketRepository ticketRepository;
    private RedisCacheService redisCacheService;
    private KafkaEventProducer kafkaEventProducer;
    private ObjectMapper objectMapper;
    private PaymentService paymentService;

    private Ticket sampleTicket;

    @BeforeEach
    void setUp() {
        paymentRepository = Mockito.mock(PaymentTransactionRepository.class);
        idempotencyRepository = Mockito.mock(IdempotencyRecordRepository.class);
        ticketRepository = Mockito.mock(TicketRepository.class);
        redisCacheService = Mockito.mock(RedisCacheService.class);
        kafkaEventProducer = Mockito.mock(KafkaEventProducer.class);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        paymentService = new PaymentService(
                paymentRepository,
                idempotencyRepository,
                ticketRepository,
                redisCacheService,
                kafkaEventProducer,
                objectMapper
        );

        Station src = new Station(1L, "WFD", "Whitefield", "ವೈಟ್‌ಫೀಲ್ಡ್", MetroLine.PURPLE, 37, 46.0, false, 12.99, 77.76);
        Station dest = new Station(2L, "IDN", "Indiranagar", "ಇಂದಿರಾನಗರ", MetroLine.PURPLE, 22, 26.1, false, 12.97, 77.63);

        sampleTicket = new Ticket();
        sampleTicket.setId(100L);
        sampleTicket.setTicketNumber("NMM-WFD-IDN-998811");
        sampleTicket.setUserId("9876543210");
        sampleTicket.setSourceStation(src);
        sampleTicket.setDestinationStation(dest);
        sampleTicket.setFareAmount(40.0);
        sampleTicket.setFinalAmount(40.0);
        sampleTicket.setPassengerCount(1);
        sampleTicket.setStatus(TicketStatus.PAYMENT_PENDING);
        sampleTicket.setQrCodeData("{}");
        sampleTicket.setCreatedAt(LocalDateTime.now());
        sampleTicket.setValidUntil(LocalDateTime.now().plusHours(4));
    }

    @Test
    void testFirstTimePaymentSuccess() {
        String idemKey = "IDEM_KEY_123456";
        PaymentRequestDTO request = new PaymentRequestDTO(sampleTicket.getTicketNumber(), PaymentMethod.UPI, idemKey, "user@okaxis");

        when(redisCacheService.acquireIdempotencyLock(idemKey)).thenReturn(true);
        when(idempotencyRepository.findByIdempotencyKey(idemKey)).thenReturn(Optional.empty());
        when(ticketRepository.findByTicketNumber(sampleTicket.getTicketNumber())).thenReturn(Optional.of(sampleTicket));

        PaymentResponseDTO response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals(sampleTicket.getTicketNumber(), response.getTicketNumber());
        assertEquals(idemKey, response.getIdempotencyKey());
        assertFalse(response.isDuplicateIgnored());

        verify(paymentRepository, times(1)).save(any(PaymentTransaction.class));
        verify(kafkaEventProducer, times(1)).sendPaymentCompletedEvent(any());
        verify(idempotencyRepository, times(1)).save(any(IdempotencyRecord.class));
    }

    @Test
    void testDuplicatePaymentIdempotentResponse() throws Exception {
        String idemKey = "IDEM_KEY_DUPLICATE_TEST";
        PaymentRequestDTO request = new PaymentRequestDTO(sampleTicket.getTicketNumber(), PaymentMethod.UPI, idemKey, "user@okaxis");

        PaymentResponseDTO cachedResponse = new PaymentResponseDTO();
        cachedResponse.setTransactionId("TXN_EXISTING_123");
        cachedResponse.setIdempotencyKey(idemKey);
        cachedResponse.setTicketNumber(sampleTicket.getTicketNumber());
        cachedResponse.setAmount(40.0);
        cachedResponse.setStatus(PaymentStatus.SUCCESS);
        cachedResponse.setDuplicateIgnored(false);

        String jsonPayload = objectMapper.writeValueAsString(cachedResponse);
        IdempotencyRecord record = new IdempotencyRecord(idemKey, "PAYMENT", null, jsonPayload, 200);

        when(redisCacheService.acquireIdempotencyLock(idemKey)).thenReturn(true);
        when(idempotencyRepository.findByIdempotencyKey(idemKey)).thenReturn(Optional.of(record));

        PaymentResponseDTO duplicateResponse = paymentService.processPayment(request);

        assertNotNull(duplicateResponse);
        assertEquals("TXN_EXISTING_123", duplicateResponse.getTransactionId());
        assertTrue(duplicateResponse.isDuplicateIgnored());
        assertTrue(duplicateResponse.getMessage().contains("Duplicate request suppressed"));

        // Guarantee ZERO duplicate saves & ZERO duplicate Kafka events
        verify(paymentRepository, never()).save(any(PaymentTransaction.class));
        verify(kafkaEventProducer, never()).sendPaymentCompletedEvent(any());
    }
}
