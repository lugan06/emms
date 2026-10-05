package com.eems.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFile store(MultipartFile file);

    void remove(StoredFile storedFile);

    record StoredFile(String originalName, String storageName, String fileUrl,
                      String contentType, long size, String sha256) {
    }
}
