package com.takeout.dto.admin;
import jakarta.validation.constraints.*;
public record PlatformConfigUpdateRequest(@NotBlank @Size(max=500) String value){}
