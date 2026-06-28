package com.takeout.dto.app;

import java.math.BigDecimal;

public record DishResponse(
        Long id,
        Long shopId,
        Long categoryId,
        String name,
        String imageUrl,
        String description,
        BigDecimal price,
        Integer stock,
        Integer salesCount,
        Integer status
) {
}
