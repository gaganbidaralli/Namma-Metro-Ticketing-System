package com.nammametro.event;

import java.time.LocalDateTime;

public class TicketIssuedEvent extends BaseMetroEvent {
    private static final long serialVersionUID = 1L;

    private Long ticketId;
    private String ticketNumber;
    private String userId;
    private String sourceStation;
    private String destinationStation;
    private Double amount;
    private LocalDateTime validUntil;

    public TicketIssuedEvent() {
        super("TICKET_ISSUED");
    }

    public TicketIssuedEvent(Long ticketId, String ticketNumber, String userId,
                             String sourceStation, String destinationStation,
                             Double amount, LocalDateTime validUntil) {
        super("TICKET_ISSUED");
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.userId = userId;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.amount = amount;
        this.validUntil = validUntil;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSourceStation() { return sourceStation; }
    public void setSourceStation(String sourceStation) { this.sourceStation = sourceStation; }

    public String getDestinationStation() { return destinationStation; }
    public void setDestinationStation(String destinationStation) { this.destinationStation = destinationStation; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
}
