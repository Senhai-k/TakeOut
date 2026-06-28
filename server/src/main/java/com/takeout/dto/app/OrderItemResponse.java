package com.takeout.dto.app;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long dishId,
        String dishName,
        String dishImageUrl,
        BigDecimal dishPrice,
        Integer quantity,
        String size,
        String spice,
        String notes,
        BigDecimal subtotalAmount
) {
}
