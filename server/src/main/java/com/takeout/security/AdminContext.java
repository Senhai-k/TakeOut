package com.takeout.security;

import com.takeout.common.ErrorCode;
import com.takeout.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class AdminContext {
    private final ThreadLocal<Identity> current = new ThreadLocal<>();
    public void set(Long adminId, String role, Long shopId) { current.set(new Identity(adminId, role, shopId)); }
    public Long requireShopId() {
        Identity identity = requireIdentity();
        if (!"MERCHANT_ADMIN".equals(identity.role()) || identity.shopId() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "当前账号没有商家权限");
        }
        return identity.shopId();
    }
    public Identity requireIdentity() {
        Identity identity = current.get();
        if (identity == null) throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录后台");
        return identity;
    }
    public void clear() { current.remove(); }
    public record Identity(Long adminId, String role, Long shopId) {}
}
