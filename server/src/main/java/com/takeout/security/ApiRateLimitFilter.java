package com.takeout.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeout.common.ApiResponse;
import com.takeout.common.ErrorCode;
import com.takeout.config.RateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {
    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final RateLimiter rateLimiter;

    public ApiRateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper, RateLimiter rateLimiter) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Limit limit = resolveLimit(request);
        if (!properties.isEnabled() || limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        int windowSeconds = Math.max(properties.getWindowSeconds(), 1);
        String key = clientIp(request) + ":" + limit.category();
        if (rateLimiter.tryAcquire(key, limit.maxRequests(), windowSeconds)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(429);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(windowSeconds));
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(ErrorCode.RATE_LIMITED)));
    }

    private Limit resolveLimit(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) return null;
        String uri = request.getRequestURI();
        if (uri.equals("/api/app/auth/login") || uri.equals("/api/app/auth/refresh")
                || uri.equals("/api/admin/auth/login") || uri.equals("/api/admin/auth/refresh")) {
            return new Limit("auth", Math.max(properties.getAuthMaxRequests(), 1));
        }
        if (uri.startsWith("/api/merchant/uploads/")) {
            return new Limit("upload", Math.max(properties.getUploadMaxRequests(), 1));
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        if (properties.isTrustForwardedHeaders()) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record Limit(String category, int maxRequests) {}
}
