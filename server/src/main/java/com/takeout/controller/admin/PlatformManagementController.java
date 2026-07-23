package com.takeout.controller.admin;

import com.takeout.common.*;
import com.takeout.dto.admin.*;
import com.takeout.service.PlatformManagementService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/platform")
public class PlatformManagementController {
    private final PlatformManagementService service;
    public PlatformManagementController(PlatformManagementService service){this.service=service;}
    @GetMapping("/overview") public ApiResponse<PlatformOverviewResponse> overview(){return ApiResponse.success(service.overview());}
    @GetMapping("/users") public ApiResponse<PageResult<PlatformUserResponse>> users(@RequestParam(required=false) String keyword,@RequestParam(required=false) Integer status,@RequestParam(defaultValue="1") long page,@RequestParam(defaultValue="10") long pageSize){return ApiResponse.success(service.listUsers(keyword,status,page,pageSize));}
    @PostMapping("/users/{id}/status") public ApiResponse<PlatformUserResponse> userStatus(@PathVariable Long id,@Valid @RequestBody PlatformStatusRequest r){return ApiResponse.success(service.updateUserStatus(id,r.status()));}
    @GetMapping("/shops") public ApiResponse<PageResult<PlatformShopResponse>> shops(@RequestParam(required=false) String keyword,@RequestParam(required=false) Integer status,@RequestParam(defaultValue="1") long page,@RequestParam(defaultValue="10") long pageSize){return ApiResponse.success(service.listShops(keyword,status,page,pageSize));}
    @PostMapping("/shops") public ApiResponse<PlatformShopResponse> createShop(@Valid @RequestBody PlatformShopUpsertRequest r){return ApiResponse.success(service.saveShop(null,r));}
    @PutMapping("/shops/{id}") public ApiResponse<PlatformShopResponse> updateShop(@PathVariable Long id,@Valid @RequestBody PlatformShopUpsertRequest r){return ApiResponse.success(service.saveShop(id,r));}
    @PostMapping("/shops/{id}/status") public ApiResponse<PlatformShopResponse> shopStatus(@PathVariable Long id,@Valid @RequestBody PlatformStatusRequest r){return ApiResponse.success(service.updateShopStatus(id,r.status()));}
    @GetMapping("/orders") public ApiResponse<PageResult<PlatformOrderResponse>> orders(@RequestParam(required=false) String orderNo,@RequestParam(required=false) Long shopId,@RequestParam(required=false) Integer status,@RequestParam(defaultValue="1") long page,@RequestParam(defaultValue="10") long pageSize){return ApiResponse.success(service.listOrders(orderNo,shopId,status,page,pageSize));}
    @GetMapping("/orders/{id}") public ApiResponse<PlatformOrderDetailResponse> order(@PathVariable Long id){return ApiResponse.success(service.orderDetail(id));}
    @PostMapping("/orders/{id}/cancel") public ApiResponse<PlatformOrderDetailResponse> cancelOrder(@PathVariable Long id,@Valid @RequestBody PlatformOrderCancelRequest r){return ApiResponse.success(service.cancelOrder(id,r.reason()));}
    @GetMapping("/merchant-admins") public ApiResponse<PageResult<PlatformMerchantAdminResponse>> merchantAdmins(@RequestParam(required=false) Long shopId,@RequestParam(defaultValue="1") long page,@RequestParam(defaultValue="20") long pageSize){return ApiResponse.success(service.listMerchantAdmins(shopId,page,pageSize));}
    @PostMapping("/merchant-admins/{id}/status") public ApiResponse<PlatformMerchantAdminResponse> merchantAdminStatus(@PathVariable Long id,@Valid @RequestBody PlatformStatusRequest r){return ApiResponse.success(service.updateMerchantAdminStatus(id,r.status()));}
    @PostMapping("/merchant-admins/{id}/password") public ApiResponse<PlatformMerchantAdminResponse> merchantAdminPassword(@PathVariable Long id,@Valid @RequestBody PlatformPasswordResetRequest r){return ApiResponse.success(service.resetMerchantAdminPassword(id,r.password()));}
    @GetMapping("/configs") public ApiResponse<java.util.List<PlatformConfigResponse>> configs(){return ApiResponse.success(service.listConfigs());}
    @PutMapping("/configs/{key}") public ApiResponse<PlatformConfigResponse> config(@PathVariable String key,@Valid @RequestBody PlatformConfigUpdateRequest r){return ApiResponse.success(service.updateConfig(key,r.value()));}
    @GetMapping("/audit-logs") public ApiResponse<PageResult<PlatformAuditResponse>> audits(@RequestParam(required=false) Long adminId,@RequestParam(required=false) String action,@RequestParam(required=false) String targetType,@RequestParam(defaultValue="1") long page,@RequestParam(defaultValue="20") long pageSize){return ApiResponse.success(service.listAudits(adminId,action,targetType,page,pageSize));}
}
