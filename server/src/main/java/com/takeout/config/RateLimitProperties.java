package com.takeout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "takeout.rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;
    private int windowSeconds = 60;
    private int authMaxRequests = 20;
    private int uploadMaxRequests = 30;
    private boolean trustForwardedHeaders = false;
    private String provider = "memory";
    private boolean redisFailOpen = false;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getWindowSeconds() { return windowSeconds; }
    public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }
    public int getAuthMaxRequests() { return authMaxRequests; }
    public void setAuthMaxRequests(int authMaxRequests) { this.authMaxRequests = authMaxRequests; }
    public int getUploadMaxRequests() { return uploadMaxRequests; }
    public void setUploadMaxRequests(int uploadMaxRequests) { this.uploadMaxRequests = uploadMaxRequests; }
    public boolean isTrustForwardedHeaders() { return trustForwardedHeaders; }
    public void setTrustForwardedHeaders(boolean trustForwardedHeaders) { this.trustForwardedHeaders = trustForwardedHeaders; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public boolean isRedisFailOpen() { return redisFailOpen; }
    public void setRedisFailOpen(boolean redisFailOpen) { this.redisFailOpen = redisFailOpen; }
}
