package com.eems.common.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;

public class PageQuery {

    @Min(value = 1, message = "page must be greater than 0")
    @Schema(description = "页码，从 1 开始", example = "1", defaultValue = "1")
    private long page = 1;

    @Min(value = 1, message = "pageSize must be greater than 0")
    @Max(value = 100, message = "pageSize must not be greater than 100")
    @Schema(description = "每页数量，最大 100", example = "20", defaultValue = "20")
    private long pageSize = 20;

    public long offset() {
        return (page - 1) * pageSize;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }
}
