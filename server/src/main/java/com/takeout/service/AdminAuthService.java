package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.domain.AdminAccount;
import com.takeout.dto.admin.AdminLoginRequest;
import com.takeout.dto.admin.AdminLoginResponse;
import com.takeout.exception.BusinessException;
import com.takeout.repository.AdminAccountRepository;
import com.takeout.security.JwtService;
import com.takeout.config.AuthSecurityProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AdminAuthService {

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthSecurityProperties authSecurityProperties;

    public AdminAuthService(
            AdminAccountRepository adminAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthSecurityProperties authSecurityProperties
    ) {
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authSecurityProperties = authSecurityProperties;
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public AdminLoginResponse login(AdminLoginRequest request) {
        AdminAccount adminAccount = adminAccountRepository.findLockedByUsername(request.username())
                .filter(account -> account.getStatus() == 1)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误"));
        LocalDateTime now = LocalDateTime.now();
        if (adminAccount.getLockedUntil() != null && adminAccount.getLockedUntil().isAfter(now)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录失败次数过多，请稍后再试");
        }
        if (adminAccount.getLockedUntil() != null) {
            adminAccount.setLockedUntil(null);
            adminAccount.setFailedLoginAttempts(0);
        }
        if (!passwordEncoder.matches(request.password(), adminAccount.getPasswordHash())) {
            int failedAttempts = adminAccount.getFailedLoginAttempts() + 1;
            adminAccount.setFailedLoginAttempts(failedAttempts);
            if (failedAttempts >= Math.max(authSecurityProperties.getMaxFailedAttempts(), 1)) {
                adminAccount.setLockedUntil(now.plusMinutes(Math.max(authSecurityProperties.getLockMinutes(), 1)));
            }
            adminAccount.setUpdatedAt(now);
            adminAccountRepository.save(adminAccount);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码错误");
        }
        adminAccount.setFailedLoginAttempts(0);
        adminAccount.setLockedUntil(null);
        adminAccount.setUpdatedAt(now);
        adminAccountRepository.save(adminAccount);
        int version = adminAccount.getTokenVersion();
        String token = jwtService.createAdminToken(adminAccount.getId(), adminAccount.getUsername(), adminAccount.getRole(), adminAccount.getShopId(), version);
        return new AdminLoginResponse(
                adminAccount.getId(),
                adminAccount.getUsername(),
                adminAccount.getDisplayName(),
                adminAccount.getRole(),
                adminAccount.getShopId(),
                adminAccount.getShopName(),
                token,
                jwtService.createAdminRefreshToken(adminAccount.getId(), adminAccount.getUsername(), adminAccount.getRole(), version)
        );
    }

    @Transactional
    public AdminLoginResponse refresh(String refreshToken) {
        var claims = jwtService.verify(refreshToken);
        if (!"REFRESH".equals(claims.get("tokenType")) || !(claims.get("adminId") instanceof Number id)
                || !(claims.get("tokenVersion") instanceof Number tokenVersion)) throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新凭证无效");
        AdminAccount account = adminAccountRepository.findLockedById(id.longValue()).filter(item -> item.getStatus() == 1)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "后台账号已停用"));
        if (account.getTokenVersion() != tokenVersion.intValue()) throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新凭证已失效");
        account.setTokenVersion(account.getTokenVersion() + 1);
        adminAccountRepository.save(account);
        int version = account.getTokenVersion();
        String token = jwtService.createAdminToken(account.getId(), account.getUsername(), account.getRole(), account.getShopId(), version);
        return new AdminLoginResponse(account.getId(), account.getUsername(), account.getDisplayName(), account.getRole(), account.getShopId(), account.getShopName(), token,
                jwtService.createAdminRefreshToken(account.getId(), account.getUsername(), account.getRole(), version));
    }

    @Transactional
    public void logout(String refreshToken) {
        var claims = jwtService.verify(refreshToken);
        if (!"REFRESH".equals(claims.get("tokenType")) || !(claims.get("adminId") instanceof Number id)
                || !(claims.get("tokenVersion") instanceof Number tokenVersion)) throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新凭证无效");
        AdminAccount account = adminAccountRepository.findLockedById(id.longValue())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "后台账号不存在"));
        if (account.getTokenVersion() != tokenVersion.intValue()) throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新凭证已失效");
        account.setTokenVersion(account.getTokenVersion() + 1);
        adminAccountRepository.save(account);
    }
}
