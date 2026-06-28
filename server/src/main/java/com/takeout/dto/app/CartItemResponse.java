package com.takeout.dto.app;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long shopId,
        Long dishId,
        String dishName,
        String dishImageUrl,
        BigDecimal dishPrice,
        Integer quantity,
        Boolean selected,
        BigDecimal subtotalAmount,
        String size,
        String spice,
        String notes
) {
}
