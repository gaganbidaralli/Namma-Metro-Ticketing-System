package com.nammametro.controller;

import com.nammametro.dto.TicketBookingRequest;
import com.nammametro.dto.TicketResponseDTO;
import com.nammametro.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/book")
    public ResponseEntity<TicketResponseDTO> bookTicket(@Valid @RequestBody TicketBookingRequest request) {
        TicketResponseDTO ticket = ticketService.bookTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/{ticketNumber}")
    public ResponseEntity<TicketResponseDTO> getTicket(@PathVariable String ticketNumber) {
        return ResponseEntity.ok(ticketService.getTicketByNumber(ticketNumber));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketResponseDTO>> getUserTickets(@PathVariable String userId) {
        return ResponseEntity.ok(ticketService.getUserTickets(userId));
    }
}
