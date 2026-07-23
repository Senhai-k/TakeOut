package com.takeout.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "takeout.wechat", name = "mode", havingValue = "wechat")
public class WechatConfigurationValidator implements ApplicationRunner {
    private final WechatProperties properties;

    public WechatConfigurationValidator(WechatProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (isBlank(properties.getAppId()) || isBlank(properties.getAppSecret())) {
            throw new IllegalStateException("WECHAT_APP_ID and WECHAT_APP_SECRET are required when TAKEOUT_WECHAT_MODE=wechat");
        }
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
