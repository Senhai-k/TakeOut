package com.takeout.dto.app;

import java.math.BigDecimal;

public record ProfileStatsResponse(
        long orderCount,
        BigDecimal totalSpent,
        long rewardPoints
) {
}
