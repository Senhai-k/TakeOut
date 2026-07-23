package com.takeout.dto.app;

import java.time.LocalDateTime;

public record OrderReviewResponse(Long id, Long orderId, Integer rating, String content, LocalDateTime createdAt) {}
