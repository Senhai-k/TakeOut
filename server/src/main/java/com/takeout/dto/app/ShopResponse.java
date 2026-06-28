package com.takeout.dto.app;

import java.math.BigDecimal;

public record ShopResponse(
        Long id,
        String name,
        String logoUrl,
        String notice,
        String phone,
        String address,
        BigDecimal minOrderAmount,
        BigDecimal deliveryFee,
        Integer businessStatus
) {
}
