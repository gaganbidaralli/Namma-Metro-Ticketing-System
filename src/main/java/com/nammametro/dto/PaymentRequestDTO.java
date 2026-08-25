package com.nammametro.dto;

import com.nammametro.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaymentRequestDTO {

    @NotBlank(message = "Ticket number is required")
    private String ticketNumber;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Idempotency key is required to guarantee duplicate safety")
    private String idempotencyKey;

    // Optional payment reference / UPI VPA / Card token
    private String paymentReference;

    public PaymentRequestDTO() {}

    public PaymentRequestDTO(String ticketNumber, PaymentMethod paymentMethod, String idempotencyKey, String paymentReference) {
        this.ticketNumber = ticketNumber;
        this.paymentMethod = paymentMethod;
        this.idempotencyKey = idempotencyKey;
        this.paymentReference = paymentReference;
    }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
}
