package com.takeout.controller.app;

import com.takeout.common.ApiResponse;
import com.takeout.dto.app.AuthLoginResponse;
import com.takeout.service.AppAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/auth")
public class AppAuthController {

    private final AppAuthService appAuthService;

    public AppAuthController(AppAuthService appAuthService) {
        this.appAuthService = appAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login() {
        return ApiResponse.success(appAuthService.loginDefaultUser());
    }
}
