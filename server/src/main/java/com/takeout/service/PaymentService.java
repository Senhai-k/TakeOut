package com.takeout.service;

import com.takeout.domain.Order;

public interface PaymentService {
    Order pay(Order order, String requestId);
}
