package com.takeout.service;

import com.takeout.common.ErrorCode;
import com.takeout.config.UploadProperties;
import com.takeout.dto.merchant.UploadResponse;
import com.takeout.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final UploadProperties uploadProperties;
    private final FileStorageService fileStorageService;

    public UploadService(UploadProperties uploadProperties, FileStorageService fileStorageService) {
        this.uploadProperties = uploadProperties;
        this.fileStorageService = fileStorageService;
    }

    public UploadResponse uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "请选择要上传的图片");
        }
        if (file.getSize() > uploadProperties.getMaxSizeBytes()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图片大小不能超过 " + maxSizeMb() + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !uploadProperties.getAllowedContentTypes().contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图片类型不支持");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "仅支持 jpg、jpeg、png、webp、gif 图片");
        }
        validateImageSignature(file, extension, contentType.toLowerCase(Locale.ROOT));
        String datePath = LocalDate.now().toString();
        String fileName = UUID.randomUUID() + "." + extension;
        FileStorageService.StoredFile storedFile = fileStorageService.store(file, datePath + "/" + fileName);
        return new UploadResponse(storedFile.fileName(), storedFile.url());
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图片文件名缺少后缀");
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private long maxSizeMb() {
        return Math.max(uploadProperties.getMaxSizeBytes() / 1024 / 1024, 1);
    }

    private void validateImageSignature(MultipartFile file, String extension, String contentType) {
        byte[] header = new byte[12];
        int length;
        try (var input = file.getInputStream()) {
            length = input.read(header);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "图片内容无法读取");
        }
        boolean valid = switch (extension) {
            case "jpg", "jpeg" -> contentType.equals("image/jpeg") && length >= 3
                    && unsigned(header[0]) == 0xff && unsigned(header[1]) == 0xd8 && unsigned(header[2]) == 0xff;
            case "png" -> contentType.equals("image/png") && length >= 8
                    && unsigned(header[0]) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G'
                    && unsigned(header[4]) == 0x0d && unsigned(header[5]) == 0x0a && unsigned(header[6]) == 0x1a && unsigned(header[7]) == 0x0a;
            case "gif" -> contentType.equals("image/gif") && length >= 6 && new String(header, 0, 6, java.nio.charset.StandardCharsets.US_ASCII).matches("GIF8[79]a");
            case "webp" -> contentType.equals("image/webp") && length >= 12
                    && ascii(header, 0, "RIFF") && ascii(header, 8, "WEBP");
            default -> false;
        };
        if (!valid) throw new BusinessException(ErrorCode.PARAM_ERROR, "图片内容与文件类型不匹配");
    }

    private int unsigned(byte value) { return value & 0xff; }

    private boolean ascii(byte[] bytes, int offset, String expected) {
        for (int index = 0; index < expected.length(); index++) {
            if (bytes[offset + index] != expected.charAt(index)) return false;
        }
        return true;
    }
}
