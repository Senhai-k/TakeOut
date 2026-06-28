package com.takeout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "takeout.upload")
public class UploadProperties {

    private String dir = "uploads";
    private long maxSizeBytes = 2 * 1024 * 1024;
    private List<String> allowedContentTypes = List.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public void setMaxSizeBytes(long maxSizeBytes) {
        this.maxSizeBytes = maxSizeBytes;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }

    public void setAllowedContentTypes(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }
}
