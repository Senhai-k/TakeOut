package com.takeout.dto.merchant;

import java.math.BigDecimal;

public record MerchantStatisticsResponse(
        long todayOrderCount,
        BigDecimal todaySalesAmount,
        long pendingOrderCount,
        long dishCount
) {
}
