package com.takeout.dto.merchant;

import jakarta.validation.constraints.NotNull;

public record MerchantUpdateOrderStatusRequest(
        @NotNull(message = "订单状态不能为空")
        Integer status
) {
}
