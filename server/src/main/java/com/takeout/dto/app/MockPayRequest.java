package com.takeout.dto.app;

import jakarta.validation.constraints.Size;

public record MockPayRequest(@Size(max = 64, message = "支付请求号过长") String requestId) {}
