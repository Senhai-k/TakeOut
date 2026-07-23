package com.takeout.dto.admin;
import jakarta.validation.constraints.*;
public record PlatformOrderCancelRequest(@NotBlank @Size(max=200) String reason){}
