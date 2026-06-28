package com.takeout.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MerchantDishUpsertRequest(
        @NotNull(message = "分类不能为空")
        Long categoryId,

        @NotBlank(message = "商品名称不能为空")
        String name,

        String imageUrl,

        String description,

        @NotNull(message = "价格不能为空")
        BigDecimal price,

        @NotNull(message = "库存不能为空")
        Integer stock,

        @NotNull(message = "状态不能为空")
        Integer status
) {
}
