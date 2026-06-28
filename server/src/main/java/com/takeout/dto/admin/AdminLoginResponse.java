package com.takeout.dto.admin;

public record AdminLoginResponse(
        Long adminId,
        String username,
        String displayName,
        String role,
        String shopName,
        String token
) {
}
