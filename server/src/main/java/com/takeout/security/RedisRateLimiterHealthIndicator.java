package com.takeout.security;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "takeout.rate-limit", name = "provider", havingValue = "redis")
public class RedisRateLimiterHealthIndicator implements HealthIndicator {
    private final RedisConnectionFactory connectionFactory;

    public RedisRateLimiterHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try (var connection = connectionFactory.getConnection()) {
            String response = connection.ping();
            return "PONG".equalsIgnoreCase(response) ? Health.up().build() : Health.down().build();
        } catch (Exception exception) {
            return Health.down().build();
        }
    }
}
