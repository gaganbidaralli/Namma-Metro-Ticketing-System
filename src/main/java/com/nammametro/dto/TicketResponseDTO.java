package com.nammametro.dto;

import com.nammametro.model.Ticket;
import com.nammametro.model.TicketStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

public class TicketResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String ticketNumber;
    private String userId;
    private StationDTO sourceStation;
    private StationDTO destinationStation;
    private Double fareAmount;
    private Double discountAmount;
    private Double finalAmount;
    private Integer passengerCount;
    private TicketStatus status;
    private String qrCodeData;
    private String qrBase64Image;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Double penaltyAmount;
    private LocalDateTime createdAt;
    private LocalDateTime validUntil;
    private String message;
    private boolean fromCache;

    public TicketResponseDTO() {}

    public TicketResponseDTO(Ticket ticket) {
        if (ticket != null) {
            this.id = ticket.getId();
            this.ticketNumber = ticket.getTicketNumber();
            this.userId = ticket.getUserId();
            this.sourceStation = new StationDTO(ticket.getSourceStation());
            this.destinationStation = new StationDTO(ticket.getDestinationStation());
            this.fareAmount = ticket.getFareAmount();
            this.discountAmount = ticket.getDiscountAmount();
            this.finalAmount = ticket.getFinalAmount();
            this.passengerCount = ticket.getPassengerCount();
            this.status = ticket.getStatus();
            this.qrCodeData = ticket.getQrCodeData();
            this.entryTime = ticket.getEntryTime();
            this.exitTime = ticket.getExitTime();
            this.penaltyAmount = ticket.getPenaltyAmount();
            this.createdAt = ticket.getCreatedAt();
            this.validUntil = ticket.getValidUntil();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public StationDTO getSourceStation() { return sourceStation; }
    public void setSourceStation(StationDTO sourceStation) { this.sourceStation = sourceStation; }

    public StationDTO getDestinationStation() { return destinationStation; }
    public void setDestinationStation(StationDTO destinationStation) { this.destinationStation = destinationStation; }

    public Double getFareAmount() { return fareAmount; }
    public void setFareAmount(Double fareAmount) { this.fareAmount = fareAmount; }

    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }

    public Double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(Double finalAmount) { this.finalAmount = finalAmount; }

    public Integer getPassengerCount() { return passengerCount; }
    public void setPassengerCount(Integer passengerCount) { this.passengerCount = passengerCount; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public String getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qrCodeData) { this.qrCodeData = qrCodeData; }

    public String getQrBase64Image() { return qrBase64Image; }
    public void setQrBase64Image(String qrBase64Image) { this.qrBase64Image = qrBase64Image; }

    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }

    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }

    public Double getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(Double penaltyAmount) { this.penaltyAmount = penaltyAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isFromCache() { return fromCache; }
    public void setFromCache(boolean fromCache) { this.fromCache = fromCache; }
}
