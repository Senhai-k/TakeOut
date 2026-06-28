package com.takeout.dto.app;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        List<CartItemResponse> items,
        BigDecimal goodsAmount,
        BigDecimal deliveryFee,
        BigDecimal payAmount
) {
}
