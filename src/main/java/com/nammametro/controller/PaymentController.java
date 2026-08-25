package com.nammametro.controller;

import com.nammametro.dto.PaymentRequestDTO;
import com.nammametro.dto.PaymentResponseDTO;
import com.nammametro.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDTO> processPayment(
            @RequestHeader(value = "Idempotency-Key", required = false) String headerIdempotencyKey,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String xHeaderIdempotencyKey,
            @Valid @RequestBody PaymentRequestDTO request) {

        String finalKey = request.getIdempotencyKey();
        if (headerIdempotencyKey != null && !headerIdempotencyKey.trim().isEmpty()) {
            finalKey = headerIdempotencyKey.trim();
        } else if (xHeaderIdempotencyKey != null && !xHeaderIdempotencyKey.trim().isEmpty()) {
            finalKey = xHeaderIdempotencyKey.trim();
        }

        request.setIdempotencyKey(finalKey);
        PaymentResponseDTO response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}
