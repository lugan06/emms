package com.eems.vo;

import com.eems.entity.FileAsset;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "文件资源")
public record FileAssetVO(
        @Schema(description = "文件资源 ID", example = "1") Long id,
        @Schema(description = "原始文件名", example = "logo.png") String originalName,
        @Schema(description = "文件访问地址", example = "/uploads/550e8400-e29b-41d4-a716-446655440000.png") String fileUrl,
        @Schema(description = "文件 MIME 类型", example = "image/png") String fileType,
        @Schema(description = "文件大小，单位字节", example = "20480") Long fileSize,
        @Schema(description = "上传人 ID", example = "1", nullable = true) Long uploadedBy,
        @Schema(description = "上传时间") LocalDateTime createdAt) {

    public static FileAssetVO from(FileAsset value) {
        return new FileAssetVO(value.getId(), value.getOriginalName(), value.getFileUrl(), value.getFileType(),
                value.getFileSize(), value.getUploadedBy(), value.getCreatedAt());
    }
}
