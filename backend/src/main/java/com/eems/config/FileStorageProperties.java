package com.eems.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "eems.file")
public class FileStorageProperties {
    private String storageType = "local";
    private String localPath;
    private String publicUrlPrefix = "/uploads";
    private long maxFileSizeBytes = 10 * 1024 * 1024;

    public String getStorageType() { return storageType; }
    public void setStorageType(String storageType) { this.storageType = storageType; }
    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }
    public String getPublicUrlPrefix() { return publicUrlPrefix; }
    public void setPublicUrlPrefix(String publicUrlPrefix) { this.publicUrlPrefix = publicUrlPrefix; }
    public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
    public void setMaxFileSizeBytes(long maxFileSizeBytes) { this.maxFileSizeBytes = maxFileSizeBytes; }
}
