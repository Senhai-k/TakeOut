package com.takeout.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "takeout.wechat", name = "mode", havingValue = "dev", matchIfMissing = true)
public class DevWechatIdentityService implements WechatIdentityService {
    @Override
    public WechatIdentity exchangeCode(String code) {
        String openId = code == null || code.isBlank() ? "dev-user-1" : code.trim();
        return new WechatIdentity(openId, null);
    }
}
