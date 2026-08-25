package com.nammametro.dto;

import com.nammametro.model.GateType;
import com.nammametro.model.TicketStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GateScanResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean gateOpened; // True if turnstile opens
    private String turnstileId;
    private String stationName;
    private GateType gateType;
    private String ticketNumber;
    private TicketStatus updatedStatus;
    private Double penaltyCharged;
    private String message;
    private String ledColor; // GREEN or RED
    private LocalDateTime timestamp;
    private long validationLatencyMs;

    public GateScanResponseDTO() {}

    public boolean isGateOpened() { return gateOpened; }
    public void setGateOpened(boolean gateOpened) { this.gateOpened = gateOpened; }

    public String getTurnstileId() { return turnstileId; }
    public void setTurnstileId(String turnstileId) { this.turnstileId = turnstileId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public GateType getGateType() { return gateType; }
    public void setGateType(GateType gateType) { this.gateType = gateType; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public TicketStatus getUpdatedStatus() { return updatedStatus; }
    public void setUpdatedStatus(TicketStatus updatedStatus) { this.updatedStatus = updatedStatus; }

    public Double getPenaltyCharged() { return penaltyCharged; }
    public void setPenaltyCharged(Double penaltyCharged) { this.penaltyCharged = penaltyCharged; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLedColor() { return ledColor; }
    public void setLedColor(String ledColor) { this.ledColor = ledColor; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public long getValidationLatencyMs() { return validationLatencyMs; }
    public void setValidationLatencyMs(long validationLatencyMs) { this.validationLatencyMs = validationLatencyMs; }
}
