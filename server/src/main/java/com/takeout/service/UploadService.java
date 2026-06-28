package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.dto.merchant.UploadResponse;
import com.takeout.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final Path uploadRoot;

    public UploadService(@Value("${takeout.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public UploadResponse uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要上传的图片");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅支持 jpg、jpeg、png、webp、gif 图片");
        }
        String datePath = LocalDate.now().toString();
        String fileName = UUID.randomUUID() + "." + extension;
        Path targetDir = uploadRoot.resolve(datePath).normalize();
        Path target = targetDir.resolve(fileName).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件路径不合法");
        }
        try {
            Files.createDirectories(targetDir);
            file.transferTo(target);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片保存失败");
        }
        return new UploadResponse(fileName, "/uploads/" + datePath + "/" + fileName);
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图片文件名缺少后缀");
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
