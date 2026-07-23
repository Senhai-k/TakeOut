package com.takeout.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.takeout.common.ErrorCode;
import com.takeout.config.WechatProperties;
import com.takeout.exception.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "takeout.wechat", name = "mode", havingValue = "wechat")
public class OfficialWechatPhoneService implements WechatPhoneService {
    private final WechatProperties properties;
    private final RestClient restClient;
    private volatile CachedToken cachedToken;

    public OfficialWechatPhoneService(WechatProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl("https://api.weixin.qq.com").build();
    }

    @Override
    public String resolvePhoneNumber(String phoneCode) {
        if (phoneCode == null || phoneCode.isBlank()) throw new BusinessException(ErrorCode.PARAM_ERROR, "手机号授权 code 不能为空");
        try {
            PhoneResponse response = restClient.post()
                    .uri(builder -> builder.path("/wxa/business/getuserphonenumber")
                            .queryParam("access_token", accessToken()).build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("code", phoneCode.trim()))
                    .retrieve()
                    .body(PhoneResponse.class);
            if (response == null || response.errorCode() == null || response.errorCode() != 0
                    || response.phoneInfo() == null || isBlank(response.phoneInfo().phoneNumber())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "微信手机号授权失败");
            }
            return response.phoneInfo().phoneNumber();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信手机号服务暂不可用");
        }
    }

    private String accessToken() {
        CachedToken current = cachedToken;
        if (current != null && current.expiresAt().isAfter(Instant.now())) return current.value();
        synchronized (this) {
            current = cachedToken;
            if (current != null && current.expiresAt().isAfter(Instant.now())) return current.value();
            validateConfiguration();
            TokenResponse response = restClient.get()
                    .uri(builder -> builder.path("/cgi-bin/token")
                            .queryParam("grant_type", "client_credential")
                            .queryParam("appid", properties.getAppId())
                            .queryParam("secret", properties.getAppSecret()).build())
                    .retrieve().body(TokenResponse.class);
            if (response == null || isBlank(response.accessToken()) || response.expiresIn() == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信接口凭证获取失败");
            }
            long safeLifetime = Math.max(response.expiresIn() - 60L, 1L);
            cachedToken = new CachedToken(response.accessToken(), Instant.now().plusSeconds(safeLifetime));
            return cachedToken.value();
        }
    }

    private void validateConfiguration() {
        if (isBlank(properties.getAppId()) || isBlank(properties.getAppSecret())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录配置不完整");
        }
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }
    private record CachedToken(String value, Instant expiresAt) {}
    private record TokenResponse(@JsonProperty("access_token") String accessToken,
                                 @JsonProperty("expires_in") Long expiresIn,
                                 @JsonProperty("errcode") Integer errorCode) {}
    private record PhoneResponse(@JsonProperty("errcode") Integer errorCode,
                                 @JsonProperty("errmsg") String errorMessage,
                                 @JsonProperty("phone_info") PhoneInfo phoneInfo) {}
    private record PhoneInfo(@JsonProperty("phoneNumber") String phoneNumber,
                             @JsonProperty("purePhoneNumber") String purePhoneNumber,
                             @JsonProperty("countryCode") String countryCode) {}
}
