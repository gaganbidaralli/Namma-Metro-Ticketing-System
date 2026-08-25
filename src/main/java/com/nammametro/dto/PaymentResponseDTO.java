package com.nammametro.dto;

import com.nammametro.model.PaymentMethod;
import com.nammametro.model.PaymentStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

public class PaymentResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private String idempotencyKey;
    private String ticketNumber;
    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayReference;
    private boolean duplicateIgnored;
    private String message;
    private TicketResponseDTO issuedTicket;
    private LocalDateTime timestamp;
    private long latencyMs;

    public PaymentResponseDTO() {}

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getGatewayReference() { return gatewayReference; }
    public void setGatewayReference(String gatewayReference) { this.gatewayReference = gatewayReference; }

    public boolean isDuplicateIgnored() { return duplicateIgnored; }
    public void setDuplicateIgnored(boolean duplicateIgnored) { this.duplicateIgnored = duplicateIgnored; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public TicketResponseDTO getIssuedTicket() { return issuedTicket; }
    public void setIssuedTicket(TicketResponseDTO issuedTicket) { this.issuedTicket = issuedTicket; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long latencyMs) { this.latencyMs = latencyMs; }
}
