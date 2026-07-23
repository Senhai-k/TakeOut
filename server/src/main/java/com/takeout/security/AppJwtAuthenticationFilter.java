package com.takeout.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeout.common.ApiResponse;
import com.takeout.common.ErrorCode;
import com.takeout.exception.BusinessException;
import com.takeout.repository.AppUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AppJwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final AppUserContext userContext;
    private final ObjectMapper objectMapper;
    private final boolean allowAnonymousTestUser;
    private final AppUserRepository appUserRepository;
    public AppJwtAuthenticationFilter(JwtService jwtService, AppUserContext userContext, ObjectMapper objectMapper, AppUserRepository appUserRepository,
                                       @Value("${takeout.app.allow-anonymous-test-user:false}") boolean allowAnonymousTestUser) {
        this.jwtService = jwtService; this.userContext = userContext; this.objectMapper = objectMapper;
        this.appUserRepository = appUserRepository;
        this.allowAnonymousTestUser = allowAnonymousTestUser;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!requiresUserAuth(request)) { chain.doFilter(request, response); return; }
        try {
            String header = request.getHeader("Authorization");
            if ((header == null || !header.startsWith("Bearer ")) && allowAnonymousTestUser) {
                userContext.setUserId(1L);
                chain.doFilter(request, response);
                return;
            }
            if (header == null || !header.startsWith("Bearer ")) throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
            Map<String, Object> claims = jwtService.verify(header.substring(7));
            if (!"ACCESS".equals(claims.get("tokenType")) || !"APP_USER".equals(claims.get("role")) || !(claims.get("userId") instanceof Number userId)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户身份无效");
            }
            Long resolvedUserId = userId.longValue();
            int tokenVersion = requireTokenVersion(claims);
            appUserRepository.findById(resolvedUserId)
                    .filter(user -> user.getStatus() == 1 && user.getTokenVersion() == tokenVersion)
                    .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "用户已停用"));
            userContext.setUserId(resolvedUserId);
            chain.doFilter(request, response);
        } catch (BusinessException exception) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), exception.getMessage())));
        } finally { userContext.clear(); }
    }

    private int requireTokenVersion(Map<String, Object> claims) {
        if (!(claims.get("tokenVersion") instanceof Number tokenVersion)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Token 版本无效");
        }
        return tokenVersion.intValue();
    }

    private boolean requiresUserAuth(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/app/")) return false;
        return uri.startsWith("/api/app/addresses") || uri.startsWith("/api/app/cart") || uri.startsWith("/api/app/orders")
                || uri.equals("/api/app/auth/profile-stats") || uri.equals("/api/app/auth/phone");
    }
}
