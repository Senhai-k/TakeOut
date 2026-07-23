package com.takeout.security;

import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
@ConditionalOnProperty(prefix = "takeout.rate-limit", name = "provider", havingValue = "memory", matchIfMissing = true)
public class InMemoryRateLimiter implements RateLimiter {
    private static final long CLEANUP_INTERVAL = 256;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong requestCount = new AtomicLong();

    @Override
    public boolean tryAcquire(String key, int maxRequests, int windowSeconds) {
        long window = System.currentTimeMillis() / (Math.max(windowSeconds, 1) * 1000L);
        Bucket bucket = buckets.compute(key, (ignored, current) ->
                current == null || current.window() != window
                        ? new Bucket(window, 1)
                        : new Bucket(window, current.count() + 1));
        if (requestCount.incrementAndGet() % CLEANUP_INTERVAL == 0) {
            buckets.entrySet().removeIf(entry -> entry.getValue().window() < window - 1);
        }
        return bucket.count() <= Math.max(maxRequests, 1);
    }

    private record Bucket(long window, int count) {}
}
