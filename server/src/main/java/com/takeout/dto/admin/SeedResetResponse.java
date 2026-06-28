package com.takeout.dto.admin;

public record SeedResetResponse(
        int shops,
        int categories,
        int dishes,
        int addresses,
        int orders
) {
}
