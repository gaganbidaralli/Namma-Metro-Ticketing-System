package com.nammametro.event;

import com.nammametro.model.PaymentMethod;
import com.nammametro.model.PaymentStatus;

public class PaymentCompletedEvent extends BaseMetroEvent {
    private static final long serialVersionUID = 1L;

    private String transactionId;
    private String idempotencyKey;
    private Long ticketId;
    private String ticketNumber;
    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private String gatewayReference;

    public PaymentCompletedEvent() {
        super("PAYMENT_COMPLETED");
    }

    public PaymentCompletedEvent(String transactionId, String idempotencyKey, Long ticketId,
                                 String ticketNumber, Double amount, PaymentMethod paymentMethod,
                                 PaymentStatus status, String gatewayReference) {
        super("PAYMENT_COMPLETED");
        this.transactionId = transactionId;
        this.idempotencyKey = idempotencyKey;
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.gatewayReference = gatewayReference;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

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
}
