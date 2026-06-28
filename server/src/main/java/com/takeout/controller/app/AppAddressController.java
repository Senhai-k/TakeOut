package com.takeout.controller.app;

import com.takeout.common.ApiResponse;
import com.takeout.dto.app.AddressRequest;
import com.takeout.dto.app.AddressResponse;
import com.takeout.service.AppAddressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/app/addresses")
public class AppAddressController {

    private final AppAddressService appAddressService;

    public AppAddressController(AppAddressService appAddressService) {
        this.appAddressService = appAddressService;
    }

    @GetMapping
    public ApiResponse<List<AddressResponse>> listAddresses() {
        return ApiResponse.success(appAddressService.listAddresses());
    }

    @PostMapping
    public ApiResponse<AddressResponse> createAddress(@Valid @RequestBody AddressRequest request) {
        return ApiResponse.success(appAddressService.createAddress(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request
    ) {
        return ApiResponse.success(appAddressService.updateAddress(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        appAddressService.deleteAddress(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/default")
    public ApiResponse<AddressResponse> setDefault(@PathVariable Long id) {
        return ApiResponse.success(appAddressService.setDefault(id));
    }
}
