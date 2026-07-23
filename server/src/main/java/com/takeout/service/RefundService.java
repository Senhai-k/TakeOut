package com.takeout.service;

import com.takeout.domain.Order;

public interface RefundService {
    Order refund(Long orderId, String requestId, String reason, String source);
}
