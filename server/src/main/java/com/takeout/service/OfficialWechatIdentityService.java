package com.takeout.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.takeout.common.ErrorCode;
import com.takeout.config.WechatProperties;
import com.takeout.exception.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(prefix = "takeout.wechat", name = "mode", havingValue = "wechat")
public class OfficialWechatIdentityService implements WechatIdentityService {
    private final WechatProperties properties;
    private final RestClient restClient;

    public OfficialWechatIdentityService(WechatProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl("https://api.weixin.qq.com").build();
    }

    @Override
    public WechatIdentity exchangeCode(String code) {
        if (code == null || code.isBlank()) throw new BusinessException(ErrorCode.PARAM_ERROR, "微信登录 code 不能为空");
        if (isBlank(properties.getAppId()) || isBlank(properties.getAppSecret())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录配置不完整");
        }
        try {
            CodeSessionResponse response = restClient.get()
                    .uri(builder -> builder.path("/sns/jscode2session")
                            .queryParam("appid", properties.getAppId())
                            .queryParam("secret", properties.getAppSecret())
                            .queryParam("js_code", code.trim())
                            .queryParam("grant_type", "authorization_code")
                            .build())
                    .retrieve()
                    .body(CodeSessionResponse.class);
            if (response == null || response.errorCode() != null || isBlank(response.openId())) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED, "微信登录凭证无效");
            }
            return new WechatIdentity(response.openId(), response.unionId());
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "微信登录服务暂不可用");
        }
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }

    private record CodeSessionResponse(
            @JsonProperty("openid") String openId,
            @JsonProperty("unionid") String unionId,
            @JsonProperty("errcode") Integer errorCode,
            @JsonProperty("errmsg") String errorMessage
    ) {}
}
