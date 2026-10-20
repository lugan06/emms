package com.eems.service.impl;

import com.eems.common.exception.BusinessException;
import com.eems.config.FileStorageProperties;
import com.eems.service.FileStorageService;
import com.eems.site.SiteContextTestSupport;
import org.junit.jupiter.api.AfterEach;
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

    @AfterEach
    void clearSiteContext() {
        SiteContextTestSupport.clear();
    }

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

    @Test
    void shouldStoreMp4UnderCurrentSiteHeroVideoDirectory() throws Exception {
        SiteContextTestSupport.selectSite(1L);
        LocalFileStorageService service = new LocalFileStorageService(properties(200 * 1024 * 1024));
        MockMultipartFile file = new MockMultipartFile("file", "hero.mp4", "video/mp4", validMp4());

        FileStorageService.StoredFile stored = service.store(file);

        assertTrue(stored.storageName().startsWith("sites/1/hero-video/"));
        assertEquals("/assets/" + stored.storageName(), stored.fileUrl());
        assertTrue(Files.exists(tempDir.resolve(stored.storageName())));
        service.remove(stored);
        assertFalse(Files.exists(tempDir.resolve(stored.storageName())));
    }

    @Test
    void shouldKeepVideoDirectoriesSeparatedBySite() throws Exception {
        LocalFileStorageService service = new LocalFileStorageService(properties(200 * 1024 * 1024));
        SiteContextTestSupport.selectSite(1L);
        FileStorageService.StoredFile siteOne = service.store(
                new MockMultipartFile("file", "one.mp4", "video/mp4", validMp4()));
        SiteContextTestSupport.selectSite(2L);
        FileStorageService.StoredFile siteTwo = service.store(
                new MockMultipartFile("file", "two.mp4", "video/mp4", validMp4()));

        assertTrue(siteOne.storageName().startsWith("sites/1/hero-video/"));
        assertTrue(siteTwo.storageName().startsWith("sites/2/hero-video/"));
        assertFalse(siteOne.storageName().equals(siteTwo.storageName()));
    }

    @Test
    void shouldRejectFakeMp4AndUnsafeStoredPath() throws Exception {
        LocalFileStorageService service = new LocalFileStorageService(properties(200 * 1024 * 1024));
        SiteContextTestSupport.selectDefaultSite();

        BusinessException invalidSignature = assertThrows(BusinessException.class,
                () -> service.store(new MockMultipartFile("file", "hero.mp4", "video/mp4", new byte[]{1, 2, 3})));
        assertEquals("FILE_CONTENT_NOT_ALLOWED", invalidSignature.getCode());

        BusinessException invalidMime = assertThrows(BusinessException.class,
                () -> service.store(new MockMultipartFile("file", "hero.mp4", "application/octet-stream", validMp4())));
        assertEquals("FILE_TYPE_NOT_ALLOWED", invalidMime.getCode());

        Path outside = tempDir.getParent().resolve("eems-storage-outside-test");
        Files.deleteIfExists(outside);
        Files.writeString(outside, "must remain");
        service.remove(new FileStorageService.StoredFile("x", "../eems-storage-outside-test", "/assets/x", "video/mp4", 1, "hash"));
        assertTrue(Files.exists(outside));
        Files.deleteIfExists(outside);
    }

    private byte[] validMp4() {
        return new byte[]{0, 0, 0, 24, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'};
    }

    private FileStorageProperties properties(long maxSize) {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setLocalPath(tempDir.toString());
        properties.setPublicUrlPrefix("/assets");
        properties.setMaxFileSizeBytes(maxSize);
        return properties;
    }
}
