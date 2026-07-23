package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.domain.Order;
import com.takeout.domain.PaymentTransaction;
import com.takeout.domain.enums.OrderStatus;
import com.takeout.domain.enums.PayStatus;
import com.takeout.exception.BusinessException;
import com.takeout.repository.OrderRepository;
import com.takeout.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.time.LocalDateTime;

@Service
@ConditionalOnProperty(prefix = "takeout.payment", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentService implements PaymentService {
    private final PaymentTransactionRepository transactionRepository;
    private final OrderRepository orderRepository;
    public MockPaymentService(PaymentTransactionRepository transactionRepository, OrderRepository orderRepository) {
        this.transactionRepository = transactionRepository; this.orderRepository = orderRepository;
    }
    @Override @Transactional
    public Order pay(Order order, String requestId) {
        order = orderRepository.findLockedById(order.getId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        String resolvedRequestId = requestId == null || requestId.isBlank() ? "mock-order-" + order.getId() : requestId.trim();
        var existing = transactionRepository.findByRequestId(resolvedRequestId);
        if (existing.isPresent()) {
            if (!existing.get().getOrderId().equals(order.getId())) throw new BusinessException(ErrorCode.CONFLICT, "支付请求号已被其他订单使用");
            return orderRepository.findById(order.getId()).orElseThrow();
        }
        if (!order.getOrderStatus().equals(OrderStatus.UNPAID.getCode())) throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不能支付");
        LocalDateTime now = LocalDateTime.now();
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setRequestId(resolvedRequestId); transaction.setOrderId(order.getId()); transaction.setOrderNo(order.getOrderNo());
        transaction.setUserId(order.getUserId()); transaction.setAmount(order.getPayAmount()); transaction.setChannel("MOCK");
        transaction.setStatus("SUCCESS"); transaction.setCreatedAt(now); transaction.setUpdatedAt(now);
        transactionRepository.save(transaction);
        order.setPayStatus(PayStatus.PAID.getCode()); order.setOrderStatus(OrderStatus.PAID_WAIT_ACCEPT.getCode()); order.setUpdatedAt(now);
        return orderRepository.save(order);
    }
}
