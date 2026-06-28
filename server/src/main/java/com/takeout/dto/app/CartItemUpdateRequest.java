package com.takeout.dto.app;

import jakarta.validation.constraints.Min;

public record CartItemUpdateRequest(
        @Min(value = 1, message = "商品数量必须大于 0")
        Integer quantity,

        Boolean selected
) {
}
