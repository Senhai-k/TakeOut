package com.takeout;

import com.takeout.config.WechatConfigurationValidator;
import com.takeout.config.WechatProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WechatConfigurationValidatorTests {
    @Test
    void rejectsMissingWechatCredentials() {
        assertThrows(IllegalStateException.class,
                () -> new WechatConfigurationValidator(new WechatProperties()).run(null));
    }

    @Test
    void acceptsCompleteWechatCredentials() {
        WechatProperties properties = new WechatProperties();
        properties.setAppId("app-id");
        properties.setAppSecret("app-secret");
        assertDoesNotThrow(() -> new WechatConfigurationValidator(properties).run(null));
    }
}
