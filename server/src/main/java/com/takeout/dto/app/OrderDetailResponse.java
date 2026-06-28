package com.takeout.dto.app;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        String orderNo,
        Long shopId,
        String shopName,
        String receiverName,
        String receiverPhone,
        String receiverAddress,
        String remark,
        BigDecimal goodsAmount,
        BigDecimal deliveryFee,
        BigDecimal discountAmount,
        BigDecimal payAmount,
        Integer payStatus,
        Integer orderStatus,
        List<OrderItemResponse> items,
        LocalDateTime createdAt
) {
}
