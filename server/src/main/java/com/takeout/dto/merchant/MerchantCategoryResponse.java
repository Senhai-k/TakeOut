package com.takeout.dto.merchant;

public record MerchantCategoryResponse(
        Long id,
        Long shopId,
        String name,
        Integer sort,
        Integer status
) {
}
