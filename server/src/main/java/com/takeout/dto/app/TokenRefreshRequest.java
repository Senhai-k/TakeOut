package com.takeout.dto.app;
import jakarta.validation.constraints.NotBlank;
public record TokenRefreshRequest(@NotBlank String refreshToken){}
