package com.takeout.controller.merchant;

import com.takeout.common.ApiResponse;
import com.takeout.dto.merchant.UploadResponse;
import com.takeout.service.UploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/merchant/uploads")
public class MerchantUploadController {

    private final UploadService uploadService;

    public MerchantUploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/images")
    public ApiResponse<UploadResponse> uploadImage(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(uploadService.uploadImage(file));
    }
}
