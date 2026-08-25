package com.nammametro.dto;

import java.io.Serializable;
import java.util.List;

public class FareEstimateResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private StationDTO source;
    private StationDTO destination;
    private double baseFare;
    private double discount;
    private double totalFare;
    private int totalStations;
    private double totalDistanceKm;
    private int estimatedDurationMinutes;
    private boolean interchangeRequired;
    private String interchangeStation;
    private List<RouteSegmentDTO> routeSegments;
    private boolean fromCache;
    private long calculationLatencyMs;

    public FareEstimateResponse() {}

    public StationDTO getSource() { return source; }
    public void setSource(StationDTO source) { this.source = source; }

    public StationDTO getDestination() { return destination; }
    public void setDestination(StationDTO destination) { this.destination = destination; }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotalFare() { return totalFare; }
    public void setTotalFare(double totalFare) { this.totalFare = totalFare; }

    public int getTotalStations() { return totalStations; }
    public void setTotalStations(int totalStations) { this.totalStations = totalStations; }

    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }

    public int getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(int estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public boolean isInterchangeRequired() { return interchangeRequired; }
    public void setInterchangeRequired(boolean interchangeRequired) { this.interchangeRequired = interchangeRequired; }

    public String getInterchangeStation() { return interchangeStation; }
    public void setInterchangeStation(String interchangeStation) { this.interchangeStation = interchangeStation; }

    public List<RouteSegmentDTO> getRouteSegments() { return routeSegments; }
    public void setRouteSegments(List<RouteSegmentDTO> routeSegments) { this.routeSegments = routeSegments; }

    public boolean isFromCache() { return fromCache; }
    public void setFromCache(boolean fromCache) { this.fromCache = fromCache; }

    public long getCalculationLatencyMs() { return calculationLatencyMs; }
    public void setCalculationLatencyMs(long calculationLatencyMs) { this.calculationLatencyMs = calculationLatencyMs; }
}
