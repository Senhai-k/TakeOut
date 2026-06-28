package com.takeout.service;

import com.takeout.dto.app.AuthLoginResponse;
import com.takeout.dto.app.ProfileStatsResponse;
import com.takeout.domain.enums.OrderStatus;
import com.takeout.domain.enums.PayStatus;
import com.takeout.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AppAuthService {

    private static final Long DEFAULT_USER_ID = 1L;
    private static final int POINTS_PER_YUAN = 2;

    private final OrderRepository orderRepository;

    public AppAuthService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public AuthLoginResponse loginDefaultUser() {
        return new AuthLoginResponse(
                DEFAULT_USER_ID,
                "张三",
                "13800000000",
                "张",
                "黄金会员",
                "user-1-token"
        );
    }

    public ProfileStatsResponse getDefaultUserStats() {
        long orderCount = orderRepository.countByUserIdAndIsDeleted(DEFAULT_USER_ID, 0);
        BigDecimal totalSpent = orderRepository.sumPaidAmountByUserId(
                DEFAULT_USER_ID,
                PayStatus.PAID.getCode(),
                OrderStatus.CANCELLED.getCode()
        );
        long rewardPoints = totalSpent
                .setScale(0, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(POINTS_PER_YUAN))
                .longValue();
        return new ProfileStatsResponse(orderCount, totalSpent, rewardPoints);
    }
}
