package com.takeout.controller.admin;

import com.takeout.common.ApiResponse;
import com.takeout.dto.admin.AdminLoginRequest;
import com.takeout.dto.admin.AdminLoginResponse;
import com.takeout.dto.app.TokenRefreshRequest;
import com.takeout.service.AdminAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    public AdminAuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ApiResponse<AdminLoginResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(adminAuthService.login(request));
    }

    @PostMapping("/refresh") public ApiResponse<AdminLoginResponse> refresh(@Valid @RequestBody TokenRefreshRequest request){return ApiResponse.success(adminAuthService.refresh(request.refreshToken()));}

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody TokenRefreshRequest request) {
        adminAuthService.logout(request.refreshToken());
        return ApiResponse.success();
    }
}
