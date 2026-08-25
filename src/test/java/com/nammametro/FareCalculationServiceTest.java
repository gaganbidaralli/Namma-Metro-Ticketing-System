package com.nammametro;

import com.nammametro.dto.FareEstimateRequest;
import com.nammametro.dto.FareEstimateResponse;
import com.nammametro.model.MetroLine;
import com.nammametro.model.Station;
import com.nammametro.repository.StationRepository;
import com.nammametro.service.FareCalculationService;
import com.nammametro.service.StationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class FareCalculationServiceTest {

    private StationRepository stationRepository;
    private StationService stationService;
    private FareCalculationService fareService;

    private Station whitefield;
    private Station indiranagar;
    private Station majesticPurple;
    private Station majesticGreen;
    private Station silkInstitute;

    @BeforeEach
    void setUp() {
        stationRepository = Mockito.mock(StationRepository.class);
        stationService = Mockito.mock(StationService.class);
        fareService = new FareCalculationService(stationRepository, stationService);

        whitefield = new Station(37L, "WFD", "Whitefield (Kadugodi)", "ವೈಟ್‌ಫೀಲ್ಡ್", MetroLine.PURPLE, 37, 46.0, false, 12.99, 77.76);
        indiranagar = new Station(22L, "IDN", "Indiranagar", "ಇಂದಿರಾನಗರ", MetroLine.PURPLE, 22, 26.1, false, 12.97, 77.63);
        majesticPurple = new Station(15L, "MJC_P", "Nadaprabhu Kempegowda Station Majestic", "ಮೆಜೆಸ್ಟಿಕ್", MetroLine.PURPLE, 15, 18.5, true, 12.97, 77.57);
        majesticGreen = new Station(66L, "MJC_G", "Nadaprabhu Kempegowda Station Majestic", "ಮೆಜೆಸ್ಟಿಕ್", MetroLine.GREEN, 17, 19.5, true, 12.97, 77.57);
        silkInstitute = new Station(81L, "SKI", "Silk Institute", "ಸಿಲ್ಕ್ ಇನ್‌ಸ್ಟಿಟ್ಯೂಟ್", MetroLine.GREEN, 32, 37.8, false, 12.85, 77.53);

        when(stationService.findStationByCodeOrId("WFD")).thenReturn(Optional.of(whitefield));
        when(stationService.findStationByCodeOrId("IDN")).thenReturn(Optional.of(indiranagar));
        when(stationService.findStationByCodeOrId("SKI")).thenReturn(Optional.of(silkInstitute));

        List<Station> purpleStations = Arrays.asList(majesticPurple, indiranagar, whitefield);
        List<Station> greenStations = Arrays.asList(majesticGreen, silkInstitute);

        when(stationRepository.findByLineNameOrderBySequenceNumAsc(MetroLine.PURPLE)).thenReturn(purpleStations);
        when(stationRepository.findByLineNameOrderBySequenceNumAsc(MetroLine.GREEN)).thenReturn(greenStations);
    }

    @Test
    void testSingleLineFareCalculation() {
        FareEstimateRequest request = new FareEstimateRequest("WFD", "IDN", false, 1);
        FareEstimateResponse response = fareService.calculateFare(request);

        assertNotNull(response);
        assertEquals("Whitefield (Kadugodi)", response.getSource().getNameEn());
        assertEquals("Indiranagar", response.getDestination().getNameEn());
        assertFalse(response.isInterchangeRequired());
        assertTrue(response.getTotalFare() > 0);
        assertEquals(19.9, response.getTotalDistanceKm());
    }

    @Test
    void testInterchangeFareCalculation() {
        FareEstimateRequest request = new FareEstimateRequest("WFD", "SKI", false, 1);
        FareEstimateResponse response = fareService.calculateFare(request);

        assertNotNull(response);
        assertTrue(response.isInterchangeRequired());
        assertEquals("Nadaprabhu Kempegowda Station Majestic", response.getInterchangeStation());
        assertEquals(2, response.getRouteSegments().size());
        assertTrue(response.getTotalDistanceKm() > 40.0);
        assertEquals(60.0, response.getTotalFare()); // Maximum capped fare slab
    }

    @Test
    void testSmartCardDiscount() {
        FareEstimateRequest standardReq = new FareEstimateRequest("WFD", "IDN", false, 1);
        FareEstimateResponse standardRes = fareService.calculateFare(standardReq);

        FareEstimateRequest discountReq = new FareEstimateRequest("WFD", "IDN", true, 1);
        FareEstimateResponse discountRes = fareService.calculateFare(discountReq);

        assertTrue(discountRes.getTotalFare() < standardRes.getTotalFare());
        assertTrue(discountRes.getDiscount() > 0);
    }
}
