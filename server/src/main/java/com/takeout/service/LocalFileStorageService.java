package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.config.UploadProperties;
import com.takeout.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class LocalFileStorageService implements FileStorageService {
    private final Path uploadRoot;

    public LocalFileStorageService(UploadProperties uploadProperties) {
        this.uploadRoot = Path.of(uploadProperties.getDir()).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(MultipartFile file, String objectKey) {
        Path target = uploadRoot.resolve(objectKey).normalize();
        if (!target.startsWith(uploadRoot)) throw new BusinessException(ErrorCode.PARAM_ERROR, "文件路径不合法");
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片保存失败");
        }
        return new StoredFile(target.getFileName().toString(), "/uploads/" + objectKey.replace('\\', '/'));
    }
}
