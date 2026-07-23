package com.takeout.dto.app;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(Long id, String refundNo, BigDecimal amount, String status, String reason,
                             String source, LocalDateTime createdAt) {}
