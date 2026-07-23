package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.exception.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "takeout.wechat", name = "mode", havingValue = "dev", matchIfMissing = true)
public class DevWechatPhoneService implements WechatPhoneService {
    @Override
    public String resolvePhoneNumber(String phoneCode) {
        if (phoneCode == null || !phoneCode.matches("^1\\d{10}$")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "开发模式手机号格式不正确");
        }
        return phoneCode;
    }
}
