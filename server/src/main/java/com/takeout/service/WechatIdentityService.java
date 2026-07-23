package com.takeout.service;

public interface WechatIdentityService {
    WechatIdentity exchangeCode(String code);

    record WechatIdentity(String openId, String unionId) {}
}
