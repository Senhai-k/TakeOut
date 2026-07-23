package com.takeout.dto.admin;
import java.time.LocalDateTime;
public record PlatformUserResponse(Long id, String nickname, String phone, Integer status, LocalDateTime createdAt) {}
