package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.domain.Shop;
import com.takeout.dto.app.ShopResponse;
import com.takeout.dto.merchant.MerchantShopUpdateRequest;
import com.takeout.exception.BusinessException;
import com.takeout.repository.ShopRepository;
import com.takeout.security.AdminContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MerchantShopService {
    private final ShopRepository shopRepository;
    private final AdminContext adminContext;

    public MerchantShopService(ShopRepository shopRepository, AdminContext adminContext) { this.shopRepository = shopRepository; this.adminContext = adminContext; }

    public ShopResponse getShop() { return toResponse(findShop()); }

    @Transactional
    public ShopResponse updateShop(MerchantShopUpdateRequest request) {
        Shop shop = findShop();
        shop.setName(request.name().trim());
        shop.setLogoUrl(request.logoUrl());
        shop.setNotice(request.notice());
        shop.setPhone(request.phone());
        shop.setAddress(request.address());
        shop.setMinOrderAmount(request.minOrderAmount());
        shop.setDeliveryFee(request.deliveryFee());
        shop.setBusinessStatus(request.businessStatus());
        shop.setUpdatedAt(LocalDateTime.now());
        return toResponse(shopRepository.save(shop));
    }

    private Shop findShop() {
        return shopRepository.findById(adminContext.requireShopId())
                .filter(shop -> shop.getIsDeleted() == 0)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "店铺不存在"));
    }

    private ShopResponse toResponse(Shop shop) {
        return new ShopResponse(shop.getId(), shop.getName(), shop.getLogoUrl(), shop.getNotice(), shop.getPhone(),
                shop.getAddress(), shop.getMinOrderAmount(), shop.getDeliveryFee(), shop.getBusinessStatus());
    }
}
