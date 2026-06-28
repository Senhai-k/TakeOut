package com.takeout.dto.app;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
        @NotBlank(message = "收货人不能为空")
        String receiverName,

        @NotBlank(message = "手机号不能为空")
        String receiverPhone,

        String province,

        String city,

        String district,

        @NotBlank(message = "详细地址不能为空")
        String detail,

        String houseNumber,

        Boolean isDefault
) {
}
