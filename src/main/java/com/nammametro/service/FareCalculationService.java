package com.nammametro.service;

import com.nammametro.dto.FareEstimateRequest;
import com.nammametro.dto.FareEstimateResponse;
import com.nammametro.dto.RouteSegmentDTO;
import com.nammametro.dto.StationDTO;
import com.nammametro.model.MetroLine;
import com.nammametro.model.Station;
import com.nammametro.repository.StationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FareCalculationService {

    private static final Logger log = LoggerFactory.getLogger(FareCalculationService.class);

    private final StationRepository stationRepository;
    private final StationService stationService;

    @Value("${nammametro.fare.base-fare:10.0}")
    private double baseFare = 10.0;

    @Value("${nammametro.fare.max-fare:60.0}")
    private double maxFare = 60.0;

    @Value("${nammametro.fare.smart-card-discount-pct:5.0}")
    private double smartCardDiscountPct = 5.0;

    public FareCalculationService(StationRepository stationRepository, StationService stationService) {
        this.stationRepository = stationRepository;
        this.stationService = stationService;
    }

    public FareEstimateResponse calculateFare(FareEstimateRequest request) {
        long startTime = System.currentTimeMillis();

        Station source = stationService.findStationByCodeOrId(request.getSourceCode())
                .orElseThrow(() -> new IllegalArgumentException("Source station not found: " + request.getSourceCode()));

        Station destination = stationService.findStationByCodeOrId(request.getDestinationCode())
                .orElseThrow(() -> new IllegalArgumentException("Destination station not found: " + request.getDestinationCode()));

        FareEstimateResponse response = new FareEstimateResponse();
        response.setSource(new StationDTO(source));
        response.setDestination(new StationDTO(destination));

        if (source.getId().equals(destination.getId())) {
            response.setBaseFare(0.0);
            response.setDiscount(0.0);
            response.setTotalFare(0.0);
            response.setTotalStations(0);
            response.setTotalDistanceKm(0.0);
            response.setEstimatedDurationMinutes(0);
            response.setInterchangeRequired(false);
            response.setRouteSegments(Collections.emptyList());
            response.setCalculationLatencyMs(System.currentTimeMillis() - startTime);
            return response;
        }

        boolean sameLine = source.getLineName() == destination.getLineName();
        List<RouteSegmentDTO> segments = new ArrayList<>();
        double totalDistanceKm = 0.0;
        int totalStations = 0;
        int estimatedDuration = 0;

        if (sameLine) {
            // Direct single-line trip
            List<Station> lineStations = stationRepository.findByLineNameOrderBySequenceNumAsc(source.getLineName());
            List<Station> path = getPathOnSameLine(lineStations, source, destination);

            double dist = Math.abs(destination.getDistanceFromStart() - source.getDistanceFromStart());
            totalDistanceKm = Math.round(dist * 10.0) / 10.0;
            totalStations = path.size();
            estimatedDuration = (totalStations - 1) * 2; // ~2 min per stop

            segments.add(new RouteSegmentDTO(
                    source.getLineName(),
                    source.getNameEn(),
                    destination.getNameEn(),
                    totalStations,
                    totalDistanceKm,
                    path.stream().map(StationDTO::new).collect(Collectors.toList())
            ));

            response.setInterchangeRequired(false);
            response.setInterchangeStation(null);
        } else {
            // Interchange required (Majestic Interchange)
            response.setInterchangeRequired(true);
            response.setInterchangeStation("Nadaprabhu Kempegowda Station Majestic");

            // Leg 1: Source to Majestic
            Station majesticLeg1 = findMajesticStationOnLine(source.getLineName());
            List<Station> line1Stations = stationRepository.findByLineNameOrderBySequenceNumAsc(source.getLineName());
            List<Station> leg1Path = getPathOnSameLine(line1Stations, source, majesticLeg1);
            double dist1 = Math.abs(majesticLeg1.getDistanceFromStart() - source.getDistanceFromStart());

            segments.add(new RouteSegmentDTO(
                    source.getLineName(),
                    source.getNameEn(),
                    majesticLeg1.getNameEn(),
                    leg1Path.size(),
                    Math.round(dist1 * 10.0) / 10.0,
                    leg1Path.stream().map(StationDTO::new).collect(Collectors.toList())
            ));

            // Leg 2: Majestic to Destination
            Station majesticLeg2 = findMajesticStationOnLine(destination.getLineName());
            List<Station> line2Stations = stationRepository.findByLineNameOrderBySequenceNumAsc(destination.getLineName());
            List<Station> leg2Path = getPathOnSameLine(line2Stations, majesticLeg2, destination);
            double dist2 = Math.abs(destination.getDistanceFromStart() - majesticLeg2.getDistanceFromStart());

            segments.add(new RouteSegmentDTO(
                    destination.getLineName(),
                    majesticLeg2.getNameEn(),
                    destination.getNameEn(),
                    leg2Path.size(),
                    Math.round(dist2 * 10.0) / 10.0,
                    leg2Path.stream().map(StationDTO::new).collect(Collectors.toList())
            ));

            totalDistanceKm = Math.round((dist1 + dist2) * 10.0) / 10.0;
            totalStations = leg1Path.size() + leg2Path.size() - 1; // Majestic counted once
            estimatedDuration = (totalStations - 1) * 2 + 5; // +5 mins transfer buffer
        }

        // Fare Calculation from Slabs
        double calculatedBaseFare = computeFareForDistance(totalDistanceKm);
        double singlePassengerFare = calculatedBaseFare;
        double discount = 0.0;

        if (request.isSmartCardUser()) {
            discount = singlePassengerFare * (smartCardDiscountPct / 100.0);
            singlePassengerFare = singlePassengerFare - discount;
        }

        int passengers = Math.max(1, request.getPassengerCount());
        double totalFare = Math.round((singlePassengerFare * passengers) * 100.0) / 100.0;

        response.setBaseFare(calculatedBaseFare * passengers);
        response.setDiscount(Math.round((discount * passengers) * 100.0) / 100.0);
        response.setTotalFare(totalFare);
        response.setTotalStations(totalStations);
        response.setTotalDistanceKm(totalDistanceKm);
        response.setEstimatedDurationMinutes(estimatedDuration);
        response.setRouteSegments(segments);
        response.setFromCache(false);
        response.setCalculationLatencyMs(System.currentTimeMillis() - startTime);

        return response;
    }

    public double computeFareForDistance(double distanceKm) {
        if (distanceKm <= 2.0) return 10.0;
        if (distanceKm <= 4.0) return 15.0;
        if (distanceKm <= 6.0) return 20.0;
        if (distanceKm <= 8.0) return 25.0;
        if (distanceKm <= 12.0) return 30.0;
        if (distanceKm <= 16.0) return 35.0;
        if (distanceKm <= 20.0) return 40.0;
        if (distanceKm <= 25.0) return 45.0;
        if (distanceKm <= 30.0) return 50.0;
        return Math.min(60.0, 50.0 + Math.ceil((distanceKm - 30.0) / 5.0) * 5.0);
    }

    private List<Station> getPathOnSameLine(List<Station> allStations, Station from, Station to) {
        int fromSeq = from.getSequenceNum();
        int toSeq = to.getSequenceNum();

        if (fromSeq <= toSeq) {
            return allStations.stream()
                    .filter(s -> s.getSequenceNum() >= fromSeq && s.getSequenceNum() <= toSeq)
                    .sorted(Comparator.comparingInt(Station::getSequenceNum))
                    .collect(Collectors.toList());
        } else {
            return allStations.stream()
                    .filter(s -> s.getSequenceNum() <= fromSeq && s.getSequenceNum() >= toSeq)
                    .sorted(Comparator.comparingInt(Station::getSequenceNum).reversed())
                    .collect(Collectors.toList());
        }
    }

    private Station findMajesticStationOnLine(MetroLine line) {
        return stationRepository.findByLineNameOrderBySequenceNumAsc(line)
                .stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsInterchange()) || s.getNameEn().toLowerCase().contains("majestic"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Interchange station Majestic not configured on line: " + line));
    }
}
