package com.takeout.dto.app;

import jakarta.validation.constraints.Size;

public record AppLoginRequest(@Size(max = 64, message = "登录凭证过长") String code) {}
