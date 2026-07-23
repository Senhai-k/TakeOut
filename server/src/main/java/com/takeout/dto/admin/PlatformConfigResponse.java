package com.takeout.dto.admin;
import java.time.LocalDateTime;
public record PlatformConfigResponse(String key,String value,String description,LocalDateTime updatedAt){}
