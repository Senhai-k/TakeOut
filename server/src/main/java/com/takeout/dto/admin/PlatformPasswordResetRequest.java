package com.takeout.dto.admin;
import jakarta.validation.constraints.*;
public record PlatformPasswordResetRequest(@NotBlank @Size(min=6,max=50) String password){}
