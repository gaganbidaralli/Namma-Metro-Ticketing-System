package com.nammametro.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "stations")
public class Station implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_code", unique = true, nullable = false, length = 10)
    private String stationCode;

    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;

    @Column(name = "name_kn", nullable = false, length = 100)
    private String nameKn;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_name", nullable = false, length = 20)
    private MetroLine lineName;

    @Column(name = "sequence_num", nullable = false)
    private Integer sequenceNum;

    @Column(name = "distance_from_start", nullable = false)
    private Double distanceFromStart;

    @Column(name = "is_interchange", nullable = false)
    private Boolean isInterchange = false;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    public Station() {}

    public Station(Long id, String stationCode, String nameEn, String nameKn, MetroLine lineName,
                   Integer sequenceNum, Double distanceFromStart, Boolean isInterchange,
                   Double latitude, Double longitude) {
        this.id = id;
        this.stationCode = stationCode;
        this.nameEn = nameEn;
        this.nameKn = nameKn;
        this.lineName = lineName;
        this.sequenceNum = sequenceNum;
        this.distanceFromStart = distanceFromStart;
        this.isInterchange = isInterchange;
        this.latitude = latitude;
        this.longitude = longitude;
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
