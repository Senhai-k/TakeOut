package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.domain.AdminAccount;
import com.takeout.dto.admin.AdminLoginRequest;
import com.takeout.dto.admin.AdminLoginResponse;
import com.takeout.exception.BusinessException;
import com.takeout.repository.AdminAccountRepository;
import com.takeout.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminAuthService(
            AdminAccountRepository adminAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminAccount adminAccount = adminAccountRepository.findByUsernameAndStatus(request.username(), 1)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误"));
        if (!passwordEncoder.matches(request.password(), adminAccount.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        String token = jwtService.createAdminToken(adminAccount.getId(), adminAccount.getUsername(), adminAccount.getRole());
        return new AdminLoginResponse(
                adminAccount.getId(),
                adminAccount.getUsername(),
                adminAccount.getDisplayName(),
                adminAccount.getRole(),
                adminAccount.getShopName(),
                token
        );
    }
}
