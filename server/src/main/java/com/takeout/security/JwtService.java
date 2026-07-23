package com.takeout.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.takeout.common.ErrorCode;
import com.takeout.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expiresInSeconds;
    private final long refreshExpiresInSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${takeout.jwt.secret:takeout-secret-change-me}") String secret,
            @Value("${takeout.jwt.expires-in-seconds:86400}") long expiresInSeconds,
            @Value("${takeout.jwt.refresh-expires-in-seconds:2592000}") long refreshExpiresInSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expiresInSeconds = expiresInSeconds;
        this.refreshExpiresInSeconds = refreshExpiresInSeconds;
    }

    public String createAdminToken(Long adminId, String username, String role) {
        return createAdminToken(adminId, username, role, "PLATFORM_ADMIN".equals(role) ? null : 1L, 0);
    }

    public String createAdminToken(Long adminId, String username, String role, Long shopId) {
        return createAdminToken(adminId, username, role, shopId, 0);
    }

    public String createAdminToken(Long adminId, String username, String role, Long shopId, int tokenVersion) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("adminId", adminId); claims.put("role", role); claims.put("tokenVersion", tokenVersion);
        if (shopId != null) claims.put("shopId", shopId);
        return createToken(username, claims, "ACCESS", expiresInSeconds);
    }

    public String createAdminRefreshToken(Long adminId, String username, String role) {
        return createAdminRefreshToken(adminId, username, role, 0);
    }

    public String createAdminRefreshToken(Long adminId, String username, String role, int tokenVersion) {
        return createToken(username, Map.of("adminId", adminId, "role", role, "tokenVersion", tokenVersion), "REFRESH", refreshExpiresInSeconds);
    }

    public String createAppUserToken(Long userId, String nickname) {
        return createAppUserToken(userId, nickname, 0);
    }

    public String createAppUserToken(Long userId, String nickname, int tokenVersion) {
        return createToken(String.valueOf(userId), Map.of("userId", userId, "nickname", nickname, "role", "APP_USER", "tokenVersion", tokenVersion), "ACCESS", expiresInSeconds);
    }

    public String createAppUserRefreshToken(Long userId) {
        return createAppUserRefreshToken(userId, 0);
    }

    public String createAppUserRefreshToken(Long userId, int tokenVersion) {
        return createToken(String.valueOf(userId), Map.of("userId", userId, "role", "APP_USER", "tokenVersion", tokenVersion), "REFRESH", refreshExpiresInSeconds);
    }

    private String createToken(String subject, Map<String, Object> claims,String tokenType,long lifetimeSeconds) {
        Instant now = Instant.now();
        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", subject);
        payload.putAll(claims);
        payload.put("tokenType",tokenType);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plusSeconds(lifetimeSeconds).getEpochSecond());
        try {
            String headerPart = encodeJson(header);
            String payloadPart = encodeJson(payload);
            String signaturePart = sign(headerPart + "." + payloadPart);
            return headerPart + "." + payloadPart + "." + signaturePart;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Token 生成失败");
        }
    }

    public Map<String, Object> verify(String token) {
        try {
            String[] parts = token == null ? new String[0] : token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期");
            }
            String expected = sign(parts[0] + "." + parts[1]);
            if (!constantTimeEquals(expected, parts[2])) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期");
            }
            Map<String, Object> payload = objectMapper.readValue(URL_DECODER.decode(parts[1]), new TypeReference<>() {});
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= exp) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期");
            }
            return payload;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录已过期");
        }
    }

    private String encodeJson(Object value) throws Exception {
        return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String content) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
        return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }
}
