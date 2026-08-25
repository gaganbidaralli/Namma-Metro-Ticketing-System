package com.nammametro.dto;

import com.nammametro.model.GateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GateScanRequestDTO {

    @NotBlank(message = "QR Code data or Ticket Number is required")
    private String scanPayload;

    @NotNull(message = "Current station code is required")
    private String stationCode;

    @NotNull(message = "Gate type (ENTRY/EXIT) is required")
    private GateType gateType;

    private String turnstileId = "GATE_A1";

    public GateScanRequestDTO() {}

    public GateScanRequestDTO(String scanPayload, String stationCode, GateType gateType, String turnstileId) {
        this.scanPayload = scanPayload;
        this.stationCode = stationCode;
        this.gateType = gateType;
        this.turnstileId = turnstileId;
    }

    public String getScanPayload() { return scanPayload; }
    public void setScanPayload(String scanPayload) { this.scanPayload = scanPayload; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public GateType getGateType() { return gateType; }
    public void setGateType(GateType gateType) { this.gateType = gateType; }

    public String getTurnstileId() { return turnstileId; }
    public void setTurnstileId(String turnstileId) { this.turnstileId = turnstileId; }
}
