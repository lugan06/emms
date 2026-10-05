package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "文件资源分页查询参数")
public class FileAssetPageQuery extends PageQuery {

    @Schema(description = "文件 MIME 类型筛选", example = "image/png", nullable = true)
    private String fileType;

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
