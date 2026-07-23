package com.takeout.dto.app;

import jakarta.validation.constraints.*;

public record OrderReviewRequest(
        @NotNull(message = "评分不能为空") @Min(value = 1, message = "评分不能低于 1") @Max(value = 5, message = "评分不能高于 5") Integer rating,
        @Size(max = 500, message = "评价内容不能超过 500 字") String content
) {}
