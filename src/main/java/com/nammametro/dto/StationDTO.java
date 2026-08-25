package com.nammametro.dto;

import com.nammametro.model.MetroLine;
import com.nammametro.model.Station;

public class StationDTO {
    private Long id;
    private String stationCode;
    private String nameEn;
    private String nameKn;
    private MetroLine lineName;
    private Integer sequenceNum;
    private Double distanceFromStart;
    private Boolean isInterchange;
    private Double latitude;
    private Double longitude;

    public StationDTO() {}

    public StationDTO(Station station) {
        if (station != null) {
            this.id = station.getId();
            this.stationCode = station.getStationCode();
            this.nameEn = station.getNameEn();
            this.nameKn = station.getNameKn();
            this.lineName = station.getLineName();
            this.sequenceNum = station.getSequenceNum();
            this.distanceFromStart = station.getDistanceFromStart();
            this.isInterchange = station.getIsInterchange();
            this.latitude = station.getLatitude();
            this.longitude = station.getLongitude();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStationCode() { return stationCode; }
    public void setStationCode(String stationCode) { this.stationCode = stationCode; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameKn() { return nameKn; }
    public void setNameKn(String nameKn) { this.nameKn = nameKn; }

    public MetroLine getLineName() { return lineName; }
    public void setLineName(MetroLine lineName) { this.lineName = lineName; }

    public Integer getSequenceNum() { return sequenceNum; }
    public void setSequenceNum(Integer sequenceNum) { this.sequenceNum = sequenceNum; }

    public Double getDistanceFromStart() { return distanceFromStart; }
    public void setDistanceFromStart(Double distanceFromStart) { this.distanceFromStart = distanceFromStart; }

    public Boolean getIsInterchange() { return isInterchange; }
    public void setIsInterchange(Boolean isInterchange) { this.isInterchange = isInterchange; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
