package com.nammametro.event;

import com.nammametro.model.GateType;

public class GateEntryExitEvent extends BaseMetroEvent {
    private static final long serialVersionUID = 1L;

    private Long ticketId;
    private String ticketNumber;
    private Long stationId;
    private String stationName;
    private GateType gateType;
    private String turnstileId;
    private Boolean isAllowed;
    private Double penaltyCharged;

    public GateEntryExitEvent() {
        super("GATE_EVENT");
    }

    public GateEntryExitEvent(Long ticketId, String ticketNumber, Long stationId, String stationName,
                              GateType gateType, String turnstileId, Boolean isAllowed, Double penaltyCharged) {
        super("GATE_EVENT");
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.stationId = stationId;
        this.stationName = stationName;
        this.gateType = gateType;
        this.turnstileId = turnstileId;
        this.isAllowed = isAllowed;
        this.penaltyCharged = penaltyCharged;
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public Long getStationId() { return stationId; }
    public void setStationId(Long stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public GateType getGateType() { return gateType; }
    public void setGateType(GateType gateType) { this.gateType = gateType; }

    public String getTurnstileId() { return turnstileId; }
    public void setTurnstileId(String turnstileId) { this.turnstileId = turnstileId; }

    public Boolean getIsAllowed() { return isAllowed; }
    public void setIsAllowed(Boolean isAllowed) { this.isAllowed = isAllowed; }

    public Double getPenaltyCharged() { return penaltyCharged; }
    public void setPenaltyCharged(Double penaltyCharged) { this.penaltyCharged = penaltyCharged; }
}
