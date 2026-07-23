package com.takeout.controller.app;

import com.takeout.common.ApiResponse;
import com.takeout.dto.app.AuthLoginResponse;
import com.takeout.dto.app.AppLoginRequest;
import com.takeout.dto.app.ProfileStatsResponse;
import com.takeout.dto.app.TokenRefreshRequest;
import com.takeout.dto.app.PhoneBindRequest;
import com.takeout.dto.app.PhoneBindingResponse;
import com.takeout.service.AppAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/app/auth")
public class AppAuthController {

    private final AppAuthService appAuthService;

    public AppAuthController(AppAuthService appAuthService) {
        this.appAuthService = appAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody(required = false) AppLoginRequest request) {
        return ApiResponse.success(appAuthService.login(request == null ? null : request.code()));
    }

    @PostMapping("/refresh") public ApiResponse<AuthLoginResponse> refresh(@Valid @RequestBody TokenRefreshRequest request){return ApiResponse.success(appAuthService.refresh(request.refreshToken()));}

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody TokenRefreshRequest request) {
        appAuthService.logout(request.refreshToken());
        return ApiResponse.success();
    }

    @GetMapping("/profile-stats")
    public ApiResponse<ProfileStatsResponse> profileStats() {
        return ApiResponse.success(appAuthService.getUserStats());
    }

    @PostMapping("/phone")
    public ApiResponse<PhoneBindingResponse> bindPhone(@Valid @RequestBody PhoneBindRequest request) {
        return ApiResponse.success(appAuthService.bindPhone(request.phoneCode()));
    }
}
