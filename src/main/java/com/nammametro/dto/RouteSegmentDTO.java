package com.nammametro.dto;

import com.nammametro.model.MetroLine;
import java.util.List;

public class RouteSegmentDTO {
    private MetroLine line;
    private String fromStation;
    private String toStation;
    private int stationsCount;
    private double distanceKm;
    private List<StationDTO> path;

    public RouteSegmentDTO() {}

    public RouteSegmentDTO(MetroLine line, String fromStation, String toStation, int stationsCount, double distanceKm, List<StationDTO> path) {
        this.line = line;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.stationsCount = stationsCount;
        this.distanceKm = distanceKm;
        this.path = path;
    }

    public MetroLine getLine() { return line; }
    public void setLine(MetroLine line) { this.line = line; }

    public String getFromStation() { return fromStation; }
    public void setFromStation(String fromStation) { this.fromStation = fromStation; }

    public String getToStation() { return toStation; }
    public void setToStation(String toStation) { this.toStation = toStation; }

    public int getStationsCount() { return stationsCount; }
    public void setStationsCount(int stationsCount) { this.stationsCount = stationsCount; }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }

    public List<StationDTO> getPath() { return path; }
    public void setPath(List<StationDTO> path) { this.path = path; }
}
