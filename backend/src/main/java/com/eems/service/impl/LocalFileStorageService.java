package com.eems.service.impl;

import com.eems.common.exception.BusinessException;
import com.eems.config.FileStorageProperties;
import com.eems.service.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "eems.file.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements FileStorageService {
    private static final Map<String, String> ALLOWED_TYPES = Map.ofEntries(
            Map.entry("jpg", "image/jpeg"),
            Map.entry("jpeg", "image/jpeg"),
            Map.entry("png", "image/png"),
            Map.entry("webp", "image/webp"),
            Map.entry("pdf", "application/pdf"),
            Map.entry("doc", "application/msword"),
            Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            Map.entry("xls", "application/vnd.ms-excel"),
            Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

    private final FileStorageProperties properties;
    private final Path root;

    public LocalFileStorageService(FileStorageProperties properties) {
        this.properties = properties;
        if (properties.getLocalPath() == null || properties.getLocalPath().isBlank()) {
            throw new IllegalStateException("eems.file.local-path must be configured");
        }
        this.root = Paths.get(properties.getLocalPath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new IllegalStateException("无法创建文件存储目录: " + root, exception);
        }
    }

    @Override
    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("FILE_EMPTY", "上传文件不能为空");
        }
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new BusinessException("FILE_TOO_LARGE", "文件大小不能超过 "
                    + properties.getMaxFileSizeBytes() / 1024 / 1024 + " MB");
        }

        String originalName = file.getOriginalFilename();
        String extension = extensionOf(originalName);
        String expectedType = ALLOWED_TYPES.get(extension);
        String contentType = file.getContentType();
        if (expectedType == null || contentType == null || !expectedType.equalsIgnoreCase(contentType)) {
            throw new BusinessException("FILE_TYPE_NOT_ALLOWED", "文件类型不在允许范围内");
        }
        validateFileSignature(file, extension);

        String storageName = UUID.randomUUID() + "." + extension;
        Path target = root.resolve(storageName).normalize();
        if (!target.getParent().equals(root)) {
            throw new BusinessException("FILE_NAME_INVALID", "文件名不合法");
        }

        try {
            Files.createDirectories(root);
            file.transferTo(target);
            String sha256 = sha256(target);
            return new StoredFile(originalName, storageName, buildUrl(storageName), contentType,
                    file.getSize(), sha256);
        } catch (IOException exception) {
            deleteQuietly(target);
            throw new BusinessException("FILE_STORAGE_FAILED", "文件保存失败");
        }
    }

    @Override
    public void remove(StoredFile storedFile) {
        if (storedFile == null) {
            return;
        }
        Path target = root.resolve(storedFile.storageName()).normalize();
        if (target.getParent().equals(root)) {
            deleteQuietly(target);
        }
    }

    private String extensionOf(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "";
        }
        String safeName = originalName.replace('\\', '/');
        int slash = safeName.lastIndexOf('/');
        String baseName = safeName.substring(slash + 1);
        int dot = baseName.lastIndexOf('.');
        if (dot <= 0 || dot == baseName.length() - 1) {
            return "";
        }
        return baseName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private void validateFileSignature(MultipartFile file, String extension) {
        try {
            byte[] header = file.getBytes();
            boolean valid = switch (extension) {
                case "jpg", "jpeg" -> startsWith(header, 0xFF, 0xD8, 0xFF);
                case "png" -> startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
                case "webp" -> header.length >= 12
                        && asciiAt(header, 0, "RIFF") && asciiAt(header, 8, "WEBP");
                case "pdf" -> asciiAt(header, 0, "%PDF-");
                case "doc", "xls" -> startsWith(header, 0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1);
                case "docx" -> isOfficeOpenXml(header, "word/");
                case "xlsx" -> isOfficeOpenXml(header, "xl/");
                default -> false;
            };
            if (!valid) {
                throw new BusinessException("FILE_CONTENT_NOT_ALLOWED", "文件内容与声明的文件类型不匹配");
            }
        } catch (IOException exception) {
            throw new BusinessException("FILE_READ_FAILED", "无法读取上传文件");
        }
    }

    private boolean isOfficeOpenXml(byte[] content, String expectedEntryPrefix) throws IOException {
        if (!startsWith(content, 0x50, 0x4B, 0x03, 0x04)) {
            return false;
        }
        boolean hasContentTypes = false;
        boolean hasExpectedEntry = false;
        try (java.util.zip.ZipInputStream zip = new java.util.zip.ZipInputStream(new ByteArrayInputStream(content))) {
            java.util.zip.ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String name = entry.getName();
                hasContentTypes |= "[Content_Types].xml".equals(name);
                hasExpectedEntry |= name.startsWith(expectedEntryPrefix);
            }
        }
        return hasContentTypes && hasExpectedEntry;
    }

    private boolean startsWith(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((bytes[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }

    private boolean asciiAt(byte[] bytes, int offset, String expected) {
        if (bytes.length < offset + expected.length()) {
            return false;
        }
        for (int i = 0; i < expected.length(); i++) {
            if (bytes[offset + i] != (byte) expected.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    private String buildUrl(String storageName) {
        String prefix = properties.getPublicUrlPrefix();
        if (prefix == null || prefix.isBlank()) {
            prefix = "/uploads";
        }
        return prefix.replaceAll("/+$", "") + "/" + storageName;
    }

    private String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read > 0) {
                        digest.update(buffer, 0, read);
                    }
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    private void deleteQuietly(Path target) {
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // Keep the original business error; cleanup is best effort.
        }
    }
}
