package com.nammametro.dto;

import java.io.Serializable;
import java.util.Map;

public class BenchmarkResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int totalIterations;
    
    // Redis Caching benchmark
    private double avgDbLatencyMs;
    private double avgRedisLatencyMs;
    private double redisSpeedupFactor;
    
    // Kafka Async vs Synchronous Processing benchmark
    private double avgSyncBookingLatencyMs;
    private double avgKafkaAsyncBookingLatencyMs;
    private double asyncLatencyReductionPct;

    private Map<String, Object> details;

    public BenchmarkResponseDTO() {}

    public int getTotalIterations() { return totalIterations; }
    public void setTotalIterations(int totalIterations) { this.totalIterations = totalIterations; }

    public double getAvgDbLatencyMs() { return avgDbLatencyMs; }
    public void setAvgDbLatencyMs(double avgDbLatencyMs) { this.avgDbLatencyMs = avgDbLatencyMs; }

    public double getAvgRedisLatencyMs() { return avgRedisLatencyMs; }
    public void setAvgRedisLatencyMs(double avgRedisLatencyMs) { this.avgRedisLatencyMs = avgRedisLatencyMs; }

    public double getRedisSpeedupFactor() { return redisSpeedupFactor; }
    public void setRedisSpeedupFactor(double redisSpeedupFactor) { this.redisSpeedupFactor = redisSpeedupFactor; }

    public double getAvgSyncBookingLatencyMs() { return avgSyncBookingLatencyMs; }
    public void setAvgSyncBookingLatencyMs(double avgSyncBookingLatencyMs) { this.avgSyncBookingLatencyMs = avgSyncBookingLatencyMs; }

    public double getAvgKafkaAsyncBookingLatencyMs() { return avgKafkaAsyncBookingLatencyMs; }
    public void setAvgKafkaAsyncBookingLatencyMs(double avgKafkaAsyncBookingLatencyMs) { this.avgKafkaAsyncBookingLatencyMs = avgKafkaAsyncBookingLatencyMs; }

    public double getAsyncLatencyReductionPct() { return asyncLatencyReductionPct; }
    public void setAsyncLatencyReductionPct(double asyncLatencyReductionPct) { this.asyncLatencyReductionPct = asyncLatencyReductionPct; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }
}
