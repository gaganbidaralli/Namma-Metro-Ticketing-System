package com.nammametro.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate_logs", indexes = {
    @Index(name = "idx_gate_ticket_id", columnList = "ticket_id"),
    @Index(name = "idx_gate_station_id", columnList = "station_id"),
    @Index(name = "idx_gate_timestamp", columnList = "timestamp")
})
public class GateLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private Long ticketId;

    @Column(name = "ticket_number", nullable = false, length = 64)
    private String ticketNumber;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "station_name", nullable = false, length = 100)
    private String stationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gate_type", nullable = false, length = 10)
    private GateType gateType;

    @Column(name = "turnstile_id", nullable = false, length = 32)
    private String turnstileId;

    @Column(name = "is_allowed", nullable = false)
    private Boolean isAllowed;

    @Column(name = "reason", length = 200)
    private String reason;

    @Column(name = "penalty_charged")
    private Double penaltyCharged = 0.0;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public GateLog() {}

    public GateLog(Long ticketId, String ticketNumber, Long stationId, String stationName,
                   GateType gateType, String turnstileId, Boolean isAllowed, String reason, Double penaltyCharged) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.stationId = stationId;
        this.stationName = stationName;
        this.gateType = gateType;
        this.turnstileId = turnstileId;
        this.isAllowed = isAllowed;
        this.reason = reason;
        this.penaltyCharged = penaltyCharged;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Double getPenaltyCharged() { return penaltyCharged; }
    public void setPenaltyCharged(Double penaltyCharged) { this.penaltyCharged = penaltyCharged; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
