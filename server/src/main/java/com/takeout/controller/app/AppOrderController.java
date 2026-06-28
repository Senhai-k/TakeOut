package com.takeout.controller.app;

import com.takeout.common.ApiResponse;
import com.takeout.common.PageResult;
import com.takeout.dto.app.CreateOrderRequest;
import com.takeout.dto.app.CreateOrderResponse;
import com.takeout.dto.app.OrderDetailResponse;
import com.takeout.service.AppMockService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/orders")
public class AppOrderController {

    private final AppMockService appMockService;

    public AppOrderController(AppMockService appMockService) {
        this.appMockService = appMockService;
    }

    @PostMapping
    public ApiResponse<CreateOrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.success(appMockService.createOrder(request));
    }

    @GetMapping
    public ApiResponse<PageResult<OrderDetailResponse>> listOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return ApiResponse.success(appMockService.listOrders(status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable Long id) {
        return ApiResponse.success(appMockService.getOrderDetail(id));
    }

    @PostMapping("/{id}/mock-pay")
    public ApiResponse<OrderDetailResponse> mockPay(@PathVariable Long id) {
        return ApiResponse.success(appMockService.mockPay(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<OrderDetailResponse> cancelOrder(@PathVariable Long id) {
        return ApiResponse.success(appMockService.cancelOrder(id));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<OrderDetailResponse> completeOrder(@PathVariable Long id) {
        return ApiResponse.success(appMockService.completeOrder(id));
    }
}
