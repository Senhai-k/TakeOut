package com.takeout.service;

import com.takeout.domain.Order;
import com.takeout.domain.enums.OrderStatus;
import com.takeout.dto.merchant.MerchantStatisticsResponse;
import com.takeout.repository.DishRepository;
import com.takeout.repository.OrderRepository;
import com.takeout.security.AdminContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MerchantStatisticsService {

    private final OrderRepository orderRepository;
    private final DishRepository dishRepository;
    private final AdminContext adminContext;

    public MerchantStatisticsService(OrderRepository orderRepository, DishRepository dishRepository, AdminContext adminContext) {
        this.orderRepository = orderRepository;
        this.dishRepository = dishRepository;
        this.adminContext = adminContext;
    }

    public MerchantStatisticsResponse overview() {
        Long shopId = adminContext.requireShopId();
        List<Order> orders = orderRepository.findByShopIdAndIsDeletedOrderByCreatedAtDesc(shopId, 0);
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long todayOrderCount = orders.stream()
                .filter(order -> order.getCreatedAt() != null && !order.getCreatedAt().isBefore(startOfToday))
                .filter(order -> order.getOrderStatus() >= OrderStatus.PAID_WAIT_ACCEPT.getCode())
                .count();
        BigDecimal todaySalesAmount = orders.stream()
                .filter(order -> order.getCreatedAt() != null && !order.getCreatedAt().isBefore(startOfToday))
                .filter(order -> order.getOrderStatus() >= OrderStatus.PAID_WAIT_ACCEPT.getCode())
                .map(Order::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pendingOrderCount = orders.stream()
                .filter(order -> order.getCreatedAt() != null && !order.getCreatedAt().isBefore(startOfToday))
                .filter(order -> order.getOrderStatus().equals(OrderStatus.PAID_WAIT_ACCEPT.getCode()))
                .count();
        long dishCount = dishRepository.findByShopIdAndStatusAndIsDeleted(shopId, 1, 0).size();
        return new MerchantStatisticsResponse(todayOrderCount, todaySalesAmount, pendingOrderCount, dishCount);
    }
}
