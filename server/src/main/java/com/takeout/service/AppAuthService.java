package com.takeout.service;

import com.takeout.dto.app.AuthLoginResponse;
import org.springframework.stereotype.Service;

@Service
public class AppAuthService {

    public AuthLoginResponse loginDefaultUser() {
        return new AuthLoginResponse(
                1L,
                "张三",
                "13800000000",
                "张",
                "黄金会员",
                "user-1-token"
        );
    }
}
