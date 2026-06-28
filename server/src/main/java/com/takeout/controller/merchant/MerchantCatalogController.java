package com.takeout.controller.merchant;

import com.takeout.common.ApiResponse;
import com.takeout.common.PageResult;
import com.takeout.dto.merchant.MerchantCategoryResponse;
import com.takeout.dto.merchant.MerchantCategoryUpsertRequest;
import com.takeout.dto.merchant.MerchantDishResponse;
import com.takeout.dto.merchant.MerchantDishStatusRequest;
import com.takeout.dto.merchant.MerchantDishUpsertRequest;
import com.takeout.service.MerchantCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant")
public class MerchantCatalogController {

    private final MerchantCatalogService merchantCatalogService;

    public MerchantCatalogController(MerchantCatalogService merchantCatalogService) {
        this.merchantCatalogService = merchantCatalogService;
    }

    @GetMapping("/categories")
    public ApiResponse<PageResult<MerchantCategoryResponse>> listCategories() {
        return ApiResponse.success(merchantCatalogService.listCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<MerchantCategoryResponse> createCategory(@Valid @RequestBody MerchantCategoryUpsertRequest request) {
        return ApiResponse.success(merchantCatalogService.saveCategory(null, request));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<MerchantCategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody MerchantCategoryUpsertRequest request
    ) {
        return ApiResponse.success(merchantCatalogService.saveCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        merchantCatalogService.deleteCategory(id);
        return ApiResponse.success();
    }

    @GetMapping("/dishes")
    public ApiResponse<PageResult<MerchantDishResponse>> listDishes() {
        return ApiResponse.success(merchantCatalogService.listDishes());
    }

    @PostMapping("/dishes")
    public ApiResponse<MerchantDishResponse> createDish(@Valid @RequestBody MerchantDishUpsertRequest request) {
        return ApiResponse.success(merchantCatalogService.saveDish(null, request));
    }

    @PutMapping("/dishes/{id}")
    public ApiResponse<MerchantDishResponse> updateDish(
            @PathVariable Long id,
            @Valid @RequestBody MerchantDishUpsertRequest request
    ) {
        return ApiResponse.success(merchantCatalogService.saveDish(id, request));
    }

    @DeleteMapping("/dishes/{id}")
    public ApiResponse<Void> deleteDish(@PathVariable Long id) {
        merchantCatalogService.deleteDish(id);
        return ApiResponse.success();
    }

    @PostMapping("/dishes/{id}/status")
    public ApiResponse<MerchantDishResponse> updateDishStatus(
            @PathVariable Long id,
            @Valid @RequestBody MerchantDishStatusRequest request
    ) {
        return ApiResponse.success(merchantCatalogService.updateDishStatus(id, request.status()));
    }
}
