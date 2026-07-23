package com.takeout.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "takeout.wechat")
public class WechatProperties {
    private String mode = "dev";
    private String appId;
    private String appSecret;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    public String getAppSecret() { return appSecret; }
    public void setAppSecret(String appSecret) { this.appSecret = appSecret; }
}
