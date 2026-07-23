package com.takeout.dto.admin;
import java.math.BigDecimal;
public record PlatformShopResponse(Long id, String name, String phone, String address, BigDecimal minOrderAmount,
                                   BigDecimal deliveryFee, Integer businessStatus, Integer status) {}
