package com.nammametro.dto;

import jakarta.validation.constraints.NotNull;

public class FareEstimateRequest {
    @NotNull(message = "Source station code or ID is required")
    private String sourceCode;

    @NotNull(message = "Destination station code or ID is required")
    private String destinationCode;

    private boolean smartCardUser = false;
    private int passengerCount = 1;

    public FareEstimateRequest() {}

    public FareEstimateRequest(String sourceCode, String destinationCode, boolean smartCardUser, int passengerCount) {
        this.sourceCode = sourceCode;
        this.destinationCode = destinationCode;
        this.smartCardUser = smartCardUser;
        this.passengerCount = passengerCount;
    }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }

    public String getDestinationCode() { return destinationCode; }
    public void setDestinationCode(String destinationCode) { this.destinationCode = destinationCode; }

    public boolean isSmartCardUser() { return smartCardUser; }
    public void setSmartCardUser(boolean smartCardUser) { this.smartCardUser = smartCardUser; }

    public int getPassengerCount() { return passengerCount; }
    public void setPassengerCount(int passengerCount) { this.passengerCount = passengerCount; }
}
