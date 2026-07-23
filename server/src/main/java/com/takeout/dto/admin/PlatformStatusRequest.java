package com.takeout.dto.admin;
import jakarta.validation.constraints.*;
public record PlatformStatusRequest(@NotNull @Min(0) @Max(1) Integer status) {}
