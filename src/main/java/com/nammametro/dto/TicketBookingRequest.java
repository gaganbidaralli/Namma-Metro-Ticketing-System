package com.nammametro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TicketBookingRequest {

    @NotBlank(message = "User ID / Phone number is required")
    private String userId;

    @NotNull(message = "Source station code is required")
    private String sourceCode;

    @NotNull(message = "Destination station code is required")
    private String destinationCode;

    @Min(value = 1, message = "Passenger count must be at least 1")
    private int passengerCount = 1;

    private boolean smartCardUser = false;

    // Optional client-provided idempotency key for booking initiation
    private String idempotencyKey;

    public TicketBookingRequest() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }

    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }

    public boolean isSmartCardUser() { return smartCardUser; }
    public void setSmartCardUser(boolean smartCardUser) { this.smartCardUser = smartCardUser; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
