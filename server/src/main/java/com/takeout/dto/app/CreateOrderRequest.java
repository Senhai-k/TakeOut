package com.takeout.dto.app;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "店铺 ID 不能为空")
        Long shopId,

        @NotNull(message = "地址 ID 不能为空")
        Long addressId,

        @NotEmpty(message = "购物车商品不能为空")
        List<Long> cartItemIds,

        String remark,

        @Size(max = 64, message = "下单请求号过长")
        String idempotencyKey
) {
}
