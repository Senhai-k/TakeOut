package com.takeout.dto.admin;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record PlatformShopUpsertRequest(@NotBlank @Size(max=100) String name,@Size(max=30) String phone,@Size(max=255) String address,
 @NotNull @DecimalMin("0") BigDecimal minOrderAmount,@NotNull @DecimalMin("0") BigDecimal deliveryFee,@NotNull @Min(0) @Max(1) Integer businessStatus,
 @NotNull @Min(0) @Max(1) Integer status,@Size(max=50) String adminUsername,@Size(min=6,max=50) String adminPassword){}
