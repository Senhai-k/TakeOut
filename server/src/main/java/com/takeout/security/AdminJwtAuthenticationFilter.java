package com.takeout.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeout.common.ApiResponse;
import com.takeout.common.ErrorCode;
import com.takeout.exception.BusinessException;
import com.takeout.repository.AdminAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final AdminAccountRepository adminAccountRepository;
    private final AdminContext adminContext;

    public AdminJwtAuthenticationFilter(JwtService jwtService, ObjectMapper objectMapper, AdminAccountRepository adminAccountRepository, AdminContext adminContext) {
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
        this.adminAccountRepository = adminAccountRepository;
        this.adminContext = adminContext;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!requiresAdminAuth(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String token = resolveBearerToken(request);
            var claims = jwtService.verify(token);
            String requiredRole = request.getRequestURI().startsWith("/api/platform/") ? "PLATFORM_ADMIN" : "MERCHANT_ADMIN";
            if (!"ACCESS".equals(claims.get("tokenType")) || !requiredRole.equals(claims.get("role"))) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "后台身份无效");
            }
            if (!(claims.get("adminId") instanceof Number adminId)) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "后台身份无效");
            }
            var account = adminAccountRepository.findById(adminId.longValue())
                    .filter(item -> item.getStatus() == 1 && requiredRole.equals(item.getRole())
                            && claims.get("tokenVersion") instanceof Number tokenVersion
                            && item.getTokenVersion() == tokenVersion.intValue())
                    .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "后台账号已失效"));
            if ("MERCHANT_ADMIN".equals(requiredRole)) {
                if (!(claims.get("shopId") instanceof Number shopId) || account.getShopId() == null || !account.getShopId().equals(shopId.longValue())) {
                    throw new BusinessException(ErrorCode.UNAUTHORIZED, "后台账号已失效");
                }
            }
            adminContext.set(account.getId(), account.getRole(), account.getShopId());
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            writeUnauthorized(response, exception.getMessage());
        } finally { adminContext.clear(); }
    }

    private boolean requiresAdminAuth(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/merchant/") || request.getRequestURI().startsWith("/api/platform/");
    }

    private String resolveBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录后台");
        }
        return header.substring("Bearer ".length());
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), message)));
    }
}
