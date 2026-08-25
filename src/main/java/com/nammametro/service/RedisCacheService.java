package com.nammametro.service;

import com.nammametro.dto.FareEstimateResponse;
import com.nammametro.dto.TicketResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RedisCacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${nammametro.redis.fare-ttl-seconds:86400}")
    private long fareTtlSeconds = 86400;

    @Value("${nammametro.redis.ticket-ttl-seconds:14400}")
    private long ticketTtlSeconds = 14400;

    @Value("${nammametro.redis.idempotency-ttl-seconds:300}")
    private long idempotencyTtlSeconds = 300;

    // In-memory fallback structures for zero-config local runs
    private final Map<String, CacheEntry> inMemoryCache = new ConcurrentHashMap<>();
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private static class CacheEntry {
        final Object value;
        final long expireAt;

        CacheEntry(Object value, long ttlMs) {
            this.value = value;
            this.expireAt = System.currentTimeMillis() + ttlMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAt;
        }
    }

    // --- FARE CACHING ---
    public Optional<FareEstimateResponse> getCachedFare(String sourceCode, String destCode, boolean isSmartCard, int passengers) {
        String key = String.format("metro:fare:%s:%s:%s:%d", sourceCode, destCode, isSmartCard, passengers);
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj instanceof FareEstimateResponse fareResponse) {
                cacheHits.incrementAndGet();
                fareResponse.setFromCache(true);
                return Optional.of(fareResponse);
            }
        } catch (Exception ex) {
            log.debug("Redis fare lookup fallback: {}", ex.getMessage());
        }

        // Check in-memory fallback
        CacheEntry entry = inMemoryCache.get(key);
        if (entry != null && !entry.isExpired()) {
            cacheHits.incrementAndGet();
            FareEstimateResponse fareResponse = (FareEstimateResponse) entry.value;
            fareResponse.setFromCache(true);
            return Optional.of(fareResponse);
        }

        cacheMisses.incrementAndGet();
        return Optional.empty();
    }

    public void cacheFare(String sourceCode, String destCode, boolean isSmartCard, int passengers, FareEstimateResponse response) {
        String key = String.format("metro:fare:%s:%s:%s:%d", sourceCode, destCode, isSmartCard, passengers);
        try {
            redisTemplate.opsForValue().set(key, response, Duration.ofSeconds(fareTtlSeconds));
        } catch (Exception ex) {
            log.debug("Redis cache fare fallback: {}", ex.getMessage());
        }
        inMemoryCache.put(key, new CacheEntry(response, fareTtlSeconds * 1000L));
    }

    // --- ACTIVE TICKET CACHING ---
    public Optional<TicketResponseDTO> getCachedTicket(String ticketNumber) {
        String key = "metro:ticket:" + ticketNumber;
        try {
            Object obj = redisTemplate.opsForValue().get(key);
            if (obj instanceof TicketResponseDTO ticketDTO) {
                cacheHits.incrementAndGet();
                ticketDTO.setFromCache(true);
                return Optional.of(ticketDTO);
            }
        } catch (Exception ex) {
            log.debug("Redis ticket lookup fallback: {}", ex.getMessage());
        }

        CacheEntry entry = inMemoryCache.get(key);
        if (entry != null && !entry.isExpired()) {
            cacheHits.incrementAndGet();
            TicketResponseDTO ticketDTO = (TicketResponseDTO) entry.value;
            ticketDTO.setFromCache(true);
            return Optional.of(ticketDTO);
        }

        cacheMisses.incrementAndGet();
        return Optional.empty();
    }

    public void cacheTicket(String ticketNumber, TicketResponseDTO ticket) {
        String key = "metro:ticket:" + ticketNumber;
        try {
            redisTemplate.opsForValue().set(key, ticket, Duration.ofSeconds(ticketTtlSeconds));
        } catch (Exception ex) {
            log.debug("Redis cache ticket fallback: {}", ex.getMessage());
        }
        inMemoryCache.put(key, new CacheEntry(ticket, ticketTtlSeconds * 1000L));
    }

    public void evictTicket(String ticketNumber) {
        String key = "metro:ticket:" + ticketNumber;
        try {
            redisTemplate.delete(key);
        } catch (Exception ignored) {}
        inMemoryCache.remove(key);
    }

    // --- DISTRIBUTED IDEMPOTENCY LOCKS ---
    /**
     * Atomically acquires a lock for an idempotency key.
     * @return true if lock was acquired, false if key is already active / locked
     */
    public boolean acquireIdempotencyLock(String idempotencyKey) {
        String lockKey = "idempotency:lock:" + idempotencyKey;
        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", Duration.ofSeconds(idempotencyTtlSeconds));
            if (acquired != null) {
                return acquired;
            }
        } catch (Exception ex) {
            log.debug("Redis lock fallback to in-memory: {}", ex.getMessage());
        }

        // In-memory fallback
        CacheEntry existing = inMemoryCache.get(lockKey);
        if (existing == null || existing.isExpired()) {
            inMemoryCache.put(lockKey, new CacheEntry("LOCKED", idempotencyTtlSeconds * 1000L));
            return true;
        }
        return false;
    }

    public void releaseIdempotencyLock(String idempotencyKey) {
        String lockKey = "idempotency:lock:" + idempotencyKey;
        try {
            redisTemplate.delete(lockKey);
        } catch (Exception ignored) {}
        inMemoryCache.remove(lockKey);
    }

    // --- METRICS ---
    public long getCacheHits() { return cacheHits.get(); }
    public long getCacheMisses() { return cacheMisses.get(); }
    public double getHitRatio() {
        long total = cacheHits.get() + cacheMisses.get();
        if (total == 0) return 1.0;
        return (double) cacheHits.get() / total;
    }

    public void resetMetrics() {
        cacheHits.set(0);
        cacheMisses.set(0);
    }
}
