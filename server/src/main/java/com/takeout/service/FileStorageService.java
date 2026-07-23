package com.takeout.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFile store(MultipartFile file, String objectKey);

    record StoredFile(String fileName, String url) {}
}
