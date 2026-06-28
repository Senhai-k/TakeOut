package com.takeout.dto.merchant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MerchantCategoryUpsertRequest(
        @NotBlank(message = "分类名称不能为空")
        String name,

        @NotNull(message = "排序不能为空")
        Integer sort,

        @NotNull(message = "状态不能为空")
        Integer status
) {
}
