package com.takeout.service;

import com.takeout.domain.enums.OrderStatus;
import com.takeout.repository.OrderRepository;
import com.takeout.repository.SystemConfigRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class OrderExpirationService {
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final long unpaidTimeoutMinutes;
    private final SystemConfigRepository configRepository;
    public OrderExpirationService(OrderRepository orderRepository, InventoryService inventoryService, SystemConfigRepository configRepository,
                                  @Value("${takeout.order.unpaid-timeout-minutes:15}") long unpaidTimeoutMinutes) {
        this.orderRepository = orderRepository; this.inventoryService = inventoryService; this.configRepository = configRepository; this.unpaidTimeoutMinutes = unpaidTimeoutMinutes;
    }
    @Scheduled(fixedDelayString = "${takeout.order.expiration-scan-millis:60000}")
    @Transactional
    public void closeExpiredOrders() {
        long timeout = configRepository.findById("order.unpaid_timeout_minutes").map(config -> {
            try { return Long.parseLong(config.getConfigValue()); } catch (NumberFormatException ignored) { return unpaidTimeoutMinutes; }
        }).orElse(unpaidTimeoutMinutes);
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(timeout);
        var orders = orderRepository.findByOrderStatusAndCreatedAtBeforeAndIsDeleted(OrderStatus.UNPAID.getCode(), cutoff, 0);
        for (var order : orders) {
            inventoryService.restore(order.getId());
            order.setOrderStatus(OrderStatus.CANCELLED.getCode());
            order.setRemark(appendTimeoutRemark(order.getRemark()));
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        }
    }
    private String appendTimeoutRemark(String remark) {
        return remark == null || remark.isBlank() ? "支付超时自动取消" : remark + "；支付超时自动取消";
    }
}
