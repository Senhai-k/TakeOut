package com.takeout.dto.admin;
import java.math.BigDecimal;
public record PlatformOverviewResponse(long userCount, long shopCount, long orderCount, BigDecimal paidSalesAmount) {}
