package com.nammametro.controller;

import com.nammametro.dto.GateScanRequestDTO;
import com.nammametro.dto.GateScanResponseDTO;
import com.nammametro.model.GateLog;
import com.nammametro.repository.GateLogRepository;
import com.nammametro.service.GateValidationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gates")
@CrossOrigin(origins = "*")
public class GateController {

    private final GateValidationService gateValidationService;
    private final GateLogRepository gateLogRepository;

    public GateController(GateValidationService gateValidationService, GateLogRepository gateLogRepository) {
        this.gateValidationService = gateValidationService;
        this.gateLogRepository = gateLogRepository;
    }

    @PostMapping("/scan")
    public ResponseEntity<GateScanResponseDTO> scanGate(@Valid @RequestBody GateScanRequestDTO request) {
        GateScanResponseDTO response = gateValidationService.processGateScan(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent-logs")
    public ResponseEntity<List<GateLog>> getRecentLogs() {
        return ResponseEntity.ok(gateLogRepository.findTop50ByOrderByTimestampDesc());
    }
}
