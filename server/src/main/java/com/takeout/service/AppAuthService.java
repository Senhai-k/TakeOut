package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.domain.AppUser;
import com.takeout.domain.enums.OrderStatus;
import com.takeout.domain.enums.PayStatus;
import com.takeout.dto.app.AuthLoginResponse;
import com.takeout.dto.app.ProfileStatsResponse;
import com.takeout.exception.BusinessException;
import com.takeout.repository.AppUserRepository;
import com.takeout.repository.OrderRepository;
import com.takeout.security.AppUserContext;
import com.takeout.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class AppAuthService {
    private static final int POINTS_PER_YUAN = 2;
    private final OrderRepository orderRepository;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;
    private final AppUserContext userContext;
    private final WechatIdentityService wechatIdentityService;
    private final WechatPhoneService wechatPhoneService;

    public AppAuthService(OrderRepository orderRepository, AppUserRepository appUserRepository, JwtService jwtService, AppUserContext userContext,
                          WechatIdentityService wechatIdentityService, WechatPhoneService wechatPhoneService) {
        this.orderRepository = orderRepository;
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
        this.userContext = userContext;
        this.wechatIdentityService = wechatIdentityService;
        this.wechatPhoneService = wechatPhoneService;
    }

    @Transactional
    public AuthLoginResponse login(String code) {
        WechatIdentityService.WechatIdentity identity = wechatIdentityService.exchangeCode(code);
        AppUser user = appUserRepository.findByOpenId(identity.openId()).orElseGet(() -> createUser(identity));
        if (user.getStatus() != 1) throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户已停用");
        return createLoginResponse(user);
    }

    @Transactional
    public AuthLoginResponse refresh(String refreshToken) {
        var claims = jwtService.verify(refreshToken);
        if (!"REFRESH".equals(claims.get("tokenType")) || !"APP_USER".equals(claims.get("role"))
                || !(claims.get("userId") instanceof Number id) || !(claims.get("tokenVersion") instanceof Number tokenVersion)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新凭证无效");
        }
        AppUser user = appUserRepository.findLockedById(id.longValue()).filter(item -> item.getStatus() == 1)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户已停用"));
        if (user.getTokenVersion() != tokenVersion.intValue()) throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新凭证已失效");
        user.setTokenVersion(user.getTokenVersion() + 1);
        appUserRepository.save(user);
        return createLoginResponse(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        var claims = jwtService.verify(refreshToken);
        if (!"REFRESH".equals(claims.get("tokenType")) || !(claims.get("userId") instanceof Number id)
                || !(claims.get("tokenVersion") instanceof Number tokenVersion)) throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新凭证无效");
        AppUser user = appUserRepository.findLockedById(id.longValue())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户不存在"));
        if (user.getTokenVersion() != tokenVersion.intValue()) throw new BusinessException(ErrorCode.UNAUTHORIZED, "刷新凭证已失效");
        user.setTokenVersion(user.getTokenVersion() + 1);
        appUserRepository.save(user);
    }

    public ProfileStatsResponse getUserStats() {
        Long userId = userContext.requireUserId();
        long orderCount = orderRepository.countByUserIdAndIsDeleted(userId, 0);
        BigDecimal totalSpent = orderRepository.sumPaidAmountByUserId(userId, PayStatus.PAID.getCode(), OrderStatus.CANCELLED.getCode());
        long rewardPoints = totalSpent.setScale(0, RoundingMode.DOWN).multiply(BigDecimal.valueOf(POINTS_PER_YUAN)).longValue();
        return new ProfileStatsResponse(orderCount, totalSpent, rewardPoints);
    }

    @Transactional
    public com.takeout.dto.app.PhoneBindingResponse bindPhone(String phoneCode) {
        AppUser user = appUserRepository.findLockedById(userContext.requireUserId())
                .filter(item -> item.getStatus() == 1)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户已停用"));
        user.setPhone(wechatPhoneService.resolvePhoneNumber(phoneCode));
        user.setUpdatedAt(LocalDateTime.now());
        appUserRepository.save(user);
        return new com.takeout.dto.app.PhoneBindingResponse(user.getId(), user.getPhone());
    }

    private AuthLoginResponse createLoginResponse(AppUser user) {
        int version = user.getTokenVersion();
        return new AuthLoginResponse(user.getId(), user.getNickname(), user.getPhone(), user.getNickname().substring(0, 1), "黄金会员",
                jwtService.createAppUserToken(user.getId(), user.getNickname(), version), jwtService.createAppUserRefreshToken(user.getId(), version));
    }

    private AppUser createUser(WechatIdentityService.WechatIdentity identity) {
        LocalDateTime now = LocalDateTime.now();
        AppUser user = new AppUser();
        user.setOpenId(identity.openId());
        user.setUnionId(identity.unionId());
        user.setNickname("微信用户");
        user.setStatus(1);
        user.setTokenVersion(0);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return appUserRepository.save(user);
    }
}
