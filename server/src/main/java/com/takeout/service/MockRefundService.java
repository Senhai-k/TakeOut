package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.domain.Order;
import com.takeout.domain.Refund;
import com.takeout.domain.enums.OrderStatus;
import com.takeout.domain.enums.PayStatus;
import com.takeout.exception.BusinessException;
import com.takeout.repository.OrderRepository;
import com.takeout.repository.PaymentTransactionRepository;
import com.takeout.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@ConditionalOnProperty(prefix = "takeout.payment", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockRefundService implements RefundService {
    private static final DateTimeFormatter REFUND_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InventoryService inventoryService;

    public MockRefundService(OrderRepository orderRepository, RefundRepository refundRepository,
                             PaymentTransactionRepository paymentTransactionRepository, InventoryService inventoryService) {
        this.orderRepository = orderRepository;
        this.refundRepository = refundRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    public Order refund(Long orderId, String requestId, String reason, String source) {
        Order order = orderRepository.findLockedById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
        String resolvedRequestId = requestId == null || requestId.isBlank() ? "mock-refund-" + orderId : requestId.trim();
        var existingRequest = refundRepository.findByRequestId(resolvedRequestId);
        if (existingRequest.isPresent()) {
            if (!existingRequest.get().getOrderId().equals(orderId)) throw new BusinessException(ErrorCode.CONFLICT, "退款请求号已被其他订单使用");
            return order;
        }
        if (refundRepository.findByOrderId(orderId).isPresent()) return order;
        if (!order.getPayStatus().equals(PayStatus.PAID.getCode()) || order.getOrderStatus() >= OrderStatus.DELIVERING.getCode()) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不能退款");
        }

        LocalDateTime now = LocalDateTime.now();
        Refund refund = new Refund();
        refund.setRequestId(resolvedRequestId);
        refund.setRefundNo("R" + now.format(REFUND_NO_FORMATTER));
        refund.setOrderId(order.getId()); refund.setOrderNo(order.getOrderNo()); refund.setAmount(order.getPayAmount());
        refund.setSource(source); refund.setStatus("SUCCESS"); refund.setReason(normalizeReason(reason));
        refund.setCreatedAt(now); refund.setUpdatedAt(now);
        refundRepository.save(refund);

        paymentTransactionRepository.findFirstByOrderIdAndStatusOrderByIdDesc(orderId, "SUCCESS").ifPresent(transaction -> {
            transaction.setStatus("REFUNDED"); transaction.setUpdatedAt(now); paymentTransactionRepository.save(transaction);
        });
        inventoryService.restore(orderId);
        order.setPayStatus(PayStatus.REFUNDED.getCode());
        order.setOrderStatus(OrderStatus.REFUNDED.getCode());
        order.setUpdatedAt(now);
        return orderRepository.save(order);
    }

    private String normalizeReason(String reason) {
        return reason == null || reason.isBlank() ? "订单取消" : reason.trim();
    }
}
