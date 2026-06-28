package com.takeout.controller.merchant;

import com.takeout.common.ApiResponse;
import com.takeout.dto.admin.SeedResetResponse;
import com.takeout.service.SeedDataService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/seed")
public class MerchantSeedController {

    private final SeedDataService seedDataService;

    public MerchantSeedController(SeedDataService seedDataService) {
        this.seedDataService = seedDataService;
    }

    @PostMapping("/reset")
    public ApiResponse<SeedResetResponse> resetSeedData() {
        return ApiResponse.success(seedDataService.reset());
    }
}
