package com.nammametro;

import com.nammametro.dto.*;
import com.nammametro.model.GateType;
import com.nammametro.model.MetroLine;
import com.nammametro.model.Station;
import com.nammametro.model.TicketStatus;
import com.nammametro.repository.StationRepository;
import com.nammametro.repository.TicketRepository;
import com.nammametro.service.GateValidationService;
import com.nammametro.service.KafkaEventProducer;
import com.nammametro.service.PaymentService;
import com.nammametro.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TicketLifecycleIntegrationTest {

    @MockBean
    private KafkaEventProducer kafkaEventProducer;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private GateValidationService gateValidationService;

    @BeforeEach
    void setUp() {
        if (stationRepository.count() == 0) {
            Station wfd = new Station(null, "WFD", "Whitefield (Kadugodi)", "ವೈಟ್‌ಫೀಲ್ಡ್", MetroLine.PURPLE, 37, 46.0, false, 12.99, 77.76);
            Station idn = new Station(null, "IDN", "Indiranagar", "ಇಂದಿರಾನಗರ", MetroLine.PURPLE, 22, 26.1, false, 12.97, 77.63);
            stationRepository.save(wfd);
            stationRepository.save(idn);
        }
    }

    @Test
    void testCompleteTicketLifecycle() {
        // 1. Book Ticket
        TicketBookingRequest bookingReq = new TicketBookingRequest();
        bookingReq.setUserId("9876500000");
        bookingReq.setSourceCode("WFD");
        bookingReq.setDestinationCode("IDN");
        bookingReq.setPassengerCount(1);
        bookingReq.setSmartCardUser(false);

        TicketResponseDTO booked = ticketService.bookTicket(bookingReq);
        assertNotNull(booked);
        assertNotNull(booked.getTicketNumber());
        assertEquals(TicketStatus.PAYMENT_PENDING, booked.getStatus());
        assertNotNull(booked.getQrBase64Image());

        // 2. Try Gate Tap-In before Payment (Should be REJECTED)
        GateScanRequestDTO earlyTapIn = new GateScanRequestDTO(booked.getTicketNumber(), "WFD", GateType.ENTRY, "GATE_1");
        GateScanResponseDTO earlyScan = gateValidationService.processGateScan(earlyTapIn);
        assertFalse(earlyScan.isGateOpened());
        assertEquals("RED", earlyScan.getLedColor());

        // 3. Process Idempotent Payment
        PaymentRequestDTO payReq = new PaymentRequestDTO(booked.getTicketNumber(), com.nammametro.model.PaymentMethod.UPI, "IDEM_INT_TEST_001", "upi@icici");
        PaymentResponseDTO paymentRes = paymentService.processPayment(payReq);
        assertNotNull(paymentRes);
        assertEquals(com.nammametro.model.PaymentStatus.SUCCESS, paymentRes.getStatus());

        // 4. Verify Ticket Status is ACTIVE
        TicketResponseDTO activeTicket = ticketService.getTicketByNumber(booked.getTicketNumber());
        assertEquals(TicketStatus.ACTIVE, activeTicket.getStatus());

        // 5. Valid Gate Tap-In at Source Station (WFD)
        GateScanRequestDTO tapInReq = new GateScanRequestDTO(booked.getTicketNumber(), "WFD", GateType.ENTRY, "GATE_1");
        GateScanResponseDTO tapInRes = gateValidationService.processGateScan(tapInReq);
        assertTrue(tapInRes.isGateOpened());
        assertEquals("GREEN", tapInRes.getLedColor());
        assertEquals(TicketStatus.IN_TRANSIT, tapInRes.getUpdatedStatus());

        // 6. Test Anti-Passback: Double Tap-In at Entry (Should be REJECTED)
        GateScanResponseDTO passbackScan = gateValidationService.processGateScan(tapInReq);
        assertFalse(passbackScan.isGateOpened());
        assertEquals("RED", passbackScan.getLedColor());

        // 7. Valid Gate Tap-Out at Destination Station (IDN)
        GateScanRequestDTO tapOutReq = new GateScanRequestDTO(booked.getTicketNumber(), "IDN", GateType.EXIT, "GATE_2");
        GateScanResponseDTO tapOutRes = gateValidationService.processGateScan(tapOutReq);
        assertTrue(tapOutRes.isGateOpened());
        assertEquals("GREEN", tapOutRes.getLedColor());
        assertEquals(TicketStatus.COMPLETED, tapOutRes.getUpdatedStatus());

        // 8. Re-entry after completed (Should be REJECTED)
        GateScanResponseDTO reuseScan = gateValidationService.processGateScan(tapInReq);
        assertFalse(reuseScan.isGateOpened());
        assertEquals("RED", reuseScan.getLedColor());
    }
}
