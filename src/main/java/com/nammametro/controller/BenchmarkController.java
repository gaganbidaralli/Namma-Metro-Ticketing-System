package com.nammametro.controller;

import com.nammametro.dto.BenchmarkRequestDTO;
import com.nammametro.dto.BenchmarkResponseDTO;
import com.nammametro.service.BenchmarkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/benchmark")
@CrossOrigin(origins = "*")
public class BenchmarkController {

    private final BenchmarkService benchmarkService;

    public BenchmarkController(BenchmarkService benchmarkService) {
        this.benchmarkService = benchmarkService;
    }

    @PostMapping("/run")
    public ResponseEntity<BenchmarkResponseDTO> runBenchmark(@RequestBody(required = false) BenchmarkRequestDTO request) {
        if (request == null) {
            request = new BenchmarkRequestDTO(50, "WFD", "MJC_P");
        }
        return ResponseEntity.ok(benchmarkService.runBenchmark(request));
    }
}
