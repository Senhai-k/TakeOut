package com.takeout.dto.app;

import java.math.BigDecimal;

public record CreateOrderResponse(
        Long orderId,
        String orderNo,
        BigDecimal payAmount,
        Integer orderStatus
) {
}
