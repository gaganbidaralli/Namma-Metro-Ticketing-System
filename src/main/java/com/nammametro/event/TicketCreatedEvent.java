package com.nammametro.event;

public class TicketCreatedEvent extends BaseMetroEvent {
    private static final long serialVersionUID = 1L;

    private Long ticketId;
    private String ticketNumber;
    private String userId;
    private String sourceStation;
    private String destinationStation;
    private Double amount;
    private Integer passengerCount;

    public TicketCreatedEvent() {
        super("TICKET_CREATED");
    }

    public TicketCreatedEvent(Long ticketId, String ticketNumber, String userId,
                              String sourceStation, String destinationStation,
                              Double amount, Integer passengerCount) {
        super("TICKET_CREATED");
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.userId = userId;
        this.sourceStation = sourceStation;
        this.destinationStation = destinationStation;
        this.amount = amount;
        this.passengerCount = passengerCount;
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

    public Integer getPassengerCount() { return passengerCount; }
    public void setPassengerCount(Integer passengerCount) { this.passengerCount = passengerCount; }
}
