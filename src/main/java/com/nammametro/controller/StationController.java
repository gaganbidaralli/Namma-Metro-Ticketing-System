package com.nammametro.controller;

import com.nammametro.dto.StationDTO;
import com.nammametro.model.MetroLine;
import com.nammametro.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
@CrossOrigin(origins = "*")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public ResponseEntity<List<StationDTO>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/lines/{line}")
    public ResponseEntity<List<StationDTO>> getStationsByLine(@PathVariable MetroLine line) {
        return ResponseEntity.ok(stationService.getStationsByLine(line));
    }

    @GetMapping("/{codeOrId}")
    public ResponseEntity<StationDTO> getStationByCodeOrId(@PathVariable String codeOrId) {
        return stationService.findStationByCodeOrId(codeOrId)
                .map(s -> ResponseEntity.ok(new StationDTO(s)))
                .orElse(ResponseEntity.notFound().build());
    }
}
