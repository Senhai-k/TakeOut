package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.domain.Dish;
import com.takeout.domain.OrderItem;
import com.takeout.dto.app.OrderItemResponse;
import com.takeout.exception.BusinessException;
import com.takeout.repository.DishRepository;
import com.takeout.repository.OrderItemRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryService {
    private final DishRepository dishRepository;
    private final OrderItemRepository orderItemRepository;
    public InventoryService(DishRepository dishRepository, OrderItemRepository orderItemRepository) {
        this.dishRepository = dishRepository; this.orderItemRepository = orderItemRepository;
    }
    public void deduct(List<OrderItemResponse> items) {
        for (OrderItemResponse item : items) {
            Dish dish = dishRepository.findById(item.dishId()).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
            if (dish.getStock() < item.quantity()) throw new BusinessException(ErrorCode.CONFLICT, dish.getName() + "库存不足");
            dish.setStock(dish.getStock() - item.quantity()); dish.setUpdatedAt(LocalDateTime.now()); dishRepository.save(dish);
        }
    }
    public void restore(Long orderId) {
        for (OrderItem item : orderItemRepository.findByOrderId(orderId)) {
            dishRepository.findById(item.getDishId()).ifPresent(dish -> {
                dish.setStock(dish.getStock() + item.getQuantity()); dish.setUpdatedAt(LocalDateTime.now()); dishRepository.save(dish);
            });
        }
    }
}
