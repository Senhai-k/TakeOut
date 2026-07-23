package com.takeout.security;

import com.takeout.common.ErrorCode;
import com.takeout.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class AppUserContext {
    private final ThreadLocal<Long> currentUserId = new ThreadLocal<>();
    public void setUserId(Long userId) { currentUserId.set(userId); }
    public Long requireUserId() {
        Long userId = currentUserId.get();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        return userId;
    }
    public void clear() { currentUserId.remove(); }
}
