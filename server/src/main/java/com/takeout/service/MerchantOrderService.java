package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.common.PageResult;
import com.takeout.domain.Order;
import com.takeout.domain.OrderItem;
import com.takeout.domain.Shop;
import com.takeout.domain.enums.OrderStatus;
import com.takeout.dto.app.OrderDetailResponse;
import com.takeout.dto.app.OrderItemResponse;
import com.takeout.exception.BusinessException;
import com.takeout.repository.OrderItemRepository;
import com.takeout.repository.OrderRepository;
import com.takeout.repository.ShopRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantOrderService {

    private static final Long DEFAULT_SHOP_ID = 1L;

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ShopRepository shopRepository;

    public MerchantOrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ShopRepository shopRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.shopRepository = shopRepository;
    }

    public PageResult<OrderDetailResponse> listOrders(Integer status, String orderNo, long page, long pageSize) {
        List<Order> orders = status == null
                ? orderRepository.findByShopIdAndIsDeletedOrderByCreatedAtDesc(DEFAULT_SHOP_ID, 0)
                : orderRepository.findByShopIdAndOrderStatusAndIsDeletedOrderByCreatedAtDesc(
                        DEFAULT_SHOP_ID,
                        status,
                        0
                );
        if (orderNo != null && !orderNo.isBlank()) {
            orders = orders.stream()
                    .filter(order -> order.getOrderNo().contains(orderNo.trim()))
                    .toList();
        }
        long safePage = Math.max(page, 1);
        long safePageSize = Math.max(pageSize, 1);
        int fromIndex = (int) Math.min((safePage - 1) * safePageSize, orders.size());
        int toIndex = (int) Math.min(fromIndex + safePageSize, orders.size());
        List<OrderDetailResponse> records = orders.subList(fromIndex, toIndex)
                .stream()
                .map(this::toOrderDetailResponse)
                .toList();
        return new PageResult<>(records, orders.size(), safePage, safePageSize);
    }

    public OrderDetailResponse getOrderDetail(Long id) {
        return toOrderDetailResponse(getOrder(id));
    }

    @Transactional
    public OrderDetailResponse acceptOrder(Long id) {
        Order order = getOrder(id);
        requireStatus(order, OrderStatus.PAID_WAIT_ACCEPT);
        order.setOrderStatus(OrderStatus.ACCEPTED.getCode());
        order.setUpdatedAt(LocalDateTime.now());
        return toOrderDetailResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderDetailResponse rejectOrder(Long id, String reason) {
        Order order = getOrder(id);
        requireStatus(order, OrderStatus.PAID_WAIT_ACCEPT);
        order.setOrderStatus(OrderStatus.CANCELLED.getCode());
        order.setRemark(appendRejectReason(order.getRemark(), reason));
        order.setUpdatedAt(LocalDateTime.now());
        return toOrderDetailResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderDetailResponse updateStatus(Long id, Integer nextStatus) {
        Order order = getOrder(id);
        int currentStatus = order.getOrderStatus();
        if (!isAllowedTransition(currentStatus, nextStatus)) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态不能这样流转");
        }
        order.setOrderStatus(nextStatus);
        order.setUpdatedAt(LocalDateTime.now());
        return toOrderDetailResponse(orderRepository.save(order));
    }

    private Order getOrder(Long id) {
        return orderRepository.findById(id)
                .filter(order -> order.getIsDeleted() == 0)
                .filter(order -> order.getShopId().equals(DEFAULT_SHOP_ID))
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
    }

    private void requireStatus(Order order, OrderStatus status) {
        if (!order.getOrderStatus().equals(status.getCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "当前订单状态不能操作");
        }
    }

    private boolean isAllowedTransition(int currentStatus, int nextStatus) {
        return (currentStatus == OrderStatus.ACCEPTED.getCode() && nextStatus == OrderStatus.COOKING.getCode())
                || (currentStatus == OrderStatus.COOKING.getCode() && nextStatus == OrderStatus.DELIVERING.getCode())
                || (currentStatus == OrderStatus.DELIVERING.getCode() && nextStatus == OrderStatus.COMPLETED.getCode());
    }

    private String appendRejectReason(String remark, String reason) {
        if (reason == null || reason.isBlank()) {
            return remark;
        }
        String suffix = "商家拒单：" + reason;
        if (remark == null || remark.isBlank()) {
            return suffix;
        }
        return remark + "；" + suffix;
    }

    private OrderDetailResponse toOrderDetailResponse(Order order) {
        Shop shop = shopRepository.findById(order.getShopId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺不存在"));
        List<OrderItemResponse> items = orderItemRepository.findByOrderId(order.getId())
                .stream()
                .map(this::toOrderItemResponse)
                .toList();
        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNo(),
                order.getShopId(),
                shop.getName(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getReceiverAddress(),
                order.getRemark(),
                order.getGoodsAmount(),
                order.getDeliveryFee(),
                order.getDiscountAmount(),
                order.getPayAmount(),
                order.getPayStatus(),
                order.getOrderStatus(),
                items,
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getDishId(),
                item.getDishName(),
                item.getDishImageUrl(),
                item.getDishPrice(),
                item.getQuantity(),
                item.getSizeOption(),
                item.getSpiceOption(),
                item.getNotes(),
                item.getSubtotalAmount()
        );
    }
}
