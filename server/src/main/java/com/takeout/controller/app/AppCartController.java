package com.takeout.controller.app;

import com.takeout.common.ApiResponse;
import com.takeout.dto.app.CartItemRequest;
import com.takeout.dto.app.CartItemResponse;
import com.takeout.dto.app.CartItemUpdateRequest;
import com.takeout.dto.app.CartResponse;
import com.takeout.service.AppCartService;
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
@RequestMapping("/api/app/cart")
public class AppCartController {

    private final AppCartService appCartService;

    public AppCartController(AppCartService appCartService) {
        this.appCartService = appCartService;
    }

    @GetMapping
    public ApiResponse<CartResponse> getCart() {
        return ApiResponse.success(appCartService.getCart());
    }

    @PostMapping("/items")
    public ApiResponse<CartItemResponse> addItem(@Valid @RequestBody CartItemRequest request) {
        return ApiResponse.success(appCartService.addItem(request));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        return ApiResponse.success(appCartService.updateItem(id, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        appCartService.deleteItem(id);
        return ApiResponse.success();
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart() {
        appCartService.clearCart();
        return ApiResponse.success();
    }
}
