package com.takeout.dto.app;

public record AddressResponse(
        Long id,
        String receiverName,
        String receiverPhone,
        String province,
        String city,
        String district,
        String detail,
        String houseNumber,
        Boolean isDefault
) {
}
