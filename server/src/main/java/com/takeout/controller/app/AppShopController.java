package com.takeout.controller.app;

import com.takeout.common.ApiResponse;
import com.takeout.dto.app.ShopResponse;
import com.takeout.service.AppMockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/shop")
public class AppShopController {

    private final AppMockService appMockService;

    public AppShopController(AppMockService appMockService) {
        this.appMockService = appMockService;
    }

    @GetMapping
    public ApiResponse<ShopResponse> getShop(@RequestParam(required = false) Long shopId) {
        return ApiResponse.success(appMockService.getShop(shopId));
    }
}
