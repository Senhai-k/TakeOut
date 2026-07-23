package com.takeout.dto.admin;

public record AdminLoginResponse(
        Long adminId,
        String username,
        String displayName,
        String role,
        Long shopId,
        String shopName,
        String token,
        String refreshToken
) {
}
