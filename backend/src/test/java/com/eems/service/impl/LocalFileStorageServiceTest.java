package com.eems.service.impl;

import com.eems.common.exception.BusinessException;
import com.eems.config.FileStorageProperties;
import com.eems.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldStoreAllowedFileWithRandomNameAndPublicUrl() throws Exception {
        FileStorageProperties properties = properties(1024);
        LocalFileStorageService service = new LocalFileStorageService(properties);
        MockMultipartFile file = new MockMultipartFile("file", "..\\logo.png", "image/png",
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

        FileStorageService.StoredFile stored = service.store(file);

        assertEquals("/assets/" + stored.storageName(), stored.fileUrl());
        assertTrue(stored.storageName().endsWith(".png"));
        assertFalse(stored.storageName().contains(".."));
        assertTrue(Files.exists(tempDir.resolve(stored.storageName())));
        service.remove(stored);
        assertFalse(Files.exists(tempDir.resolve(stored.storageName())));
    }

    @Test
    void shouldCreateConfiguredDirectoryWhenItDoesNotExist() throws Exception {
        Path configuredPath = tempDir.resolve("nested").resolve("uploads");
        FileStorageProperties properties = properties(1024);
        properties.setLocalPath(configuredPath.toString());

        new LocalFileStorageService(properties);

        assertTrue(Files.isDirectory(configuredPath));
    }

    @Test
    void shouldRejectExecutableAndMimeMismatch() {
        LocalFileStorageService service = new LocalFileStorageService(properties(1024));

        assertEquals("FILE_TYPE_NOT_ALLOWED", assertThrows(BusinessException.class,
                () -> service.store(new MockMultipartFile("file", "run.exe", "application/octet-stream", new byte[]{1})))
                .getCode());
        assertEquals("FILE_TYPE_NOT_ALLOWED", assertThrows(BusinessException.class,
                () -> service.store(new MockMultipartFile("file", "image.png", "application/pdf", new byte[]{1})))
                .getCode());
        assertEquals("FILE_CONTENT_NOT_ALLOWED", assertThrows(BusinessException.class,
                () -> service.store(new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2, 3})))
                .getCode());
    }

    @Test
    void shouldRejectOversizedFile() {
        LocalFileStorageService service = new LocalFileStorageService(properties(2));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.store(new MockMultipartFile("file", "image.png", "image/png", new byte[]{1, 2, 3})));

        assertEquals("FILE_TOO_LARGE", exception.getCode());
    }

    private FileStorageProperties properties(long maxSize) {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setLocalPath(tempDir.toString());
        properties.setPublicUrlPrefix("/assets");
        properties.setMaxFileSizeBytes(maxSize);
        return properties;
    }
}
