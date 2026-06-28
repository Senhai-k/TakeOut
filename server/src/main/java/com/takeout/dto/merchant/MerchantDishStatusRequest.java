package com.takeout.dto.merchant;

import jakarta.validation.constraints.NotNull;

public record MerchantDishStatusRequest(
        @NotNull(message = "状态不能为空")
        Integer status
) {
}
