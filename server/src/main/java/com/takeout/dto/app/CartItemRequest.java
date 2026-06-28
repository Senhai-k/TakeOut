package com.takeout.dto.app;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull(message = "店铺 ID 不能为空")
        Long shopId,

        @NotNull(message = "商品 ID 不能为空")
        Long dishId,

        @Min(value = 1, message = "商品数量必须大于 0")
        Integer quantity,

        String size,

        String spice,

        String notes
) {
}
