package com.nammametro.controller;

import com.nammametro.dto.FareEstimateRequest;
import com.nammametro.dto.FareEstimateResponse;
import com.nammametro.service.FareCalculationService;
import com.nammametro.service.RedisCacheService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/fares")
@CrossOrigin(origins = "*")
public class FareController {

    private final FareCalculationService fareCalculationService;
    private final RedisCacheService redisCacheService;

    public FareController(FareCalculationService fareCalculationService, RedisCacheService redisCacheService) {
        this.fareCalculationService = fareCalculationService;
        this.redisCacheService = redisCacheService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<FareEstimateResponse> calculateFare(@Valid @RequestBody FareEstimateRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. Fast Redis Lookup
        Optional<FareEstimateResponse> cachedFare = redisCacheService.getCachedFare(
                request.getSourceCode(),
                request.getDestinationCode(),
                request.isSmartCardUser(),
                request.getPassengerCount()
        );

        if (cachedFare.isPresent()) {
            FareEstimateResponse res = cachedFare.get();
            res.setCalculationLatencyMs(System.currentTimeMillis() - startTime);
            return ResponseEntity.ok(res);
        }

        // 2. Compute Fare
        FareEstimateResponse response = fareCalculationService.calculateFare(request);

        // 3. Cache into Redis
        redisCacheService.cacheFare(
                request.getSourceCode(),
                request.getDestinationCode(),
                request.isSmartCardUser(),
                request.getPassengerCount(),
                response
        );

        return ResponseEntity.ok(response);
    }
}
