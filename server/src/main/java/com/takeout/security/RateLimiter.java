package com.takeout.security;

public interface RateLimiter {
    boolean tryAcquire(String key, int maxRequests, int windowSeconds);
}
