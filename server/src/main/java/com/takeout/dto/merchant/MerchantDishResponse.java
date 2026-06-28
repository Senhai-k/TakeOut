package com.takeout.dto.merchant;

import java.math.BigDecimal;

public record MerchantDishResponse(
        Long id,
        Long shopId,
        Long categoryId,
        String categoryName,
        String name,
        String imageUrl,
        String description,
        BigDecimal price,
        Integer stock,
        Integer salesCount,
        Integer status
) {
}
