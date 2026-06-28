package com.takeout.controller.merchant;

import com.takeout.common.ApiResponse;
import com.takeout.dto.merchant.MerchantStatisticsResponse;
import com.takeout.service.MerchantStatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/statistics")
public class MerchantStatisticsController {

    private final MerchantStatisticsService merchantStatisticsService;

    public MerchantStatisticsController(MerchantStatisticsService merchantStatisticsService) {
        this.merchantStatisticsService = merchantStatisticsService;
    }

    @GetMapping("/overview")
    public ApiResponse<MerchantStatisticsResponse> overview() {
        return ApiResponse.success(merchantStatisticsService.overview());
    }
}
