package com.takeout;

import com.takeout.config.RateLimitProperties;
import com.takeout.security.RedisRateLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisRateLimiterTests {
    @Test
    void allowsRequestsWithinLimitAndRejectsRequestsOverLimit() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.execute(any(), anyList(), any())).thenReturn(1L, 3L);
        RedisRateLimiter limiter = new RedisRateLimiter(redis, new RateLimitProperties());

        assertTrue(limiter.tryAcquire("127.0.0.1:auth", 2, 60));
        assertFalse(limiter.tryAcquire("127.0.0.1:auth", 2, 60));
    }

    @Test
    void redisFailureIsClosedByDefaultAndCanBeConfiguredOpen() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        when(redis.execute(any(), anyList(), any())).thenThrow(new RedisConnectionFailureException("unavailable"));
        RateLimitProperties properties = new RateLimitProperties();
        RedisRateLimiter limiter = new RedisRateLimiter(redis, properties);

        assertFalse(limiter.tryAcquire("127.0.0.1:auth", 2, 60));
        properties.setRedisFailOpen(true);
        assertTrue(limiter.tryAcquire("127.0.0.1:auth", 2, 60));
    }
}
