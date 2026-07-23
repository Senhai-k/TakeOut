package com.takeout.dto.app;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PhoneBindRequest(@NotBlank @Size(max = 256) String phoneCode) {}
