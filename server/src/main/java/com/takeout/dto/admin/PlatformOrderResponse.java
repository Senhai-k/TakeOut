package com.takeout.dto.admin;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public record PlatformOrderResponse(Long id, String orderNo, Long userId, Long shopId, String shopName,
                                    BigDecimal payAmount, Integer payStatus, Integer orderStatus, LocalDateTime createdAt) {}
