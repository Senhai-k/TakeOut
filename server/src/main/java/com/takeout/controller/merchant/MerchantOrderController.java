package com.takeout.controller.merchant;

import com.takeout.common.ApiResponse;
import com.takeout.common.PageResult;
import com.takeout.dto.app.OrderDetailResponse;
import com.takeout.dto.merchant.MerchantRejectOrderRequest;
import com.takeout.dto.merchant.MerchantUpdateOrderStatusRequest;
import com.takeout.service.MerchantOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant/orders")
public class MerchantOrderController {

    private final MerchantOrderService merchantOrderService;

    public MerchantOrderController(MerchantOrderService merchantOrderService) {
        this.merchantOrderService = merchantOrderService;
    }

    @GetMapping
    public ApiResponse<PageResult<OrderDetailResponse>> listOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(merchantOrderService.listOrders(status, orderNo, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable Long id) {
        return ApiResponse.success(merchantOrderService.getOrderDetail(id));
    }

    @PostMapping("/{id}/accept")
    public ApiResponse<OrderDetailResponse> acceptOrder(@PathVariable Long id) {
        return ApiResponse.success(merchantOrderService.acceptOrder(id));
    }

    @PostMapping("/{id}/reject")
    public ApiResponse<OrderDetailResponse> rejectOrder(
            @PathVariable Long id,
            @RequestBody(required = false) MerchantRejectOrderRequest request
    ) {
        String reason = request == null ? null : request.reason();
        return ApiResponse.success(merchantOrderService.rejectOrder(id, reason));
    }

    @PostMapping("/{id}/status")
    public ApiResponse<OrderDetailResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody MerchantUpdateOrderStatusRequest request
    ) {
        return ApiResponse.success(merchantOrderService.updateStatus(id, request.status()));
    }
}
