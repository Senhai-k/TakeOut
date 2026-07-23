package com.takeout.controller.merchant;

import com.takeout.common.ApiResponse;
import com.takeout.dto.app.ShopResponse;
import com.takeout.dto.merchant.MerchantShopUpdateRequest;
import com.takeout.service.MerchantShopService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchant/shop")
public class MerchantShopController {
    private final MerchantShopService merchantShopService;

    public MerchantShopController(MerchantShopService merchantShopService) { this.merchantShopService = merchantShopService; }

    @GetMapping
    public ApiResponse<ShopResponse> getShop() { return ApiResponse.success(merchantShopService.getShop()); }

    @PutMapping
    public ApiResponse<ShopResponse> updateShop(@Valid @RequestBody MerchantShopUpdateRequest request) {
        return ApiResponse.success(merchantShopService.updateShop(request));
    }
}
