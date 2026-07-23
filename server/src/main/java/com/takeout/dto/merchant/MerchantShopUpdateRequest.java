package com.takeout.dto.merchant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MerchantShopUpdateRequest(
        @NotBlank(message = "店铺名称不能为空") @Size(max = 100, message = "店铺名称不能超过 100 字") String name,
        String logoUrl,
        @Size(max = 500, message = "公告不能超过 500 字") String notice,
        @Size(max = 30, message = "联系电话不能超过 30 字") String phone,
        @Size(max = 255, message = "地址不能超过 255 字") String address,
        @NotNull(message = "起送价不能为空") @DecimalMin(value = "0.00", message = "起送价不能小于 0") BigDecimal minOrderAmount,
        @NotNull(message = "配送费不能为空") @DecimalMin(value = "0.00", message = "配送费不能小于 0") BigDecimal deliveryFee,
        @NotNull(message = "营业状态不能为空") @Min(value = 0, message = "营业状态只能为 0 或 1") @Max(value = 1, message = "营业状态只能为 0 或 1") Integer businessStatus
) {}
