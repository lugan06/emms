package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "栏目排序项")
public class CategoryReorderItem {
    @NotNull
    @Schema(description = "栏目 ID", example = "1")
    private Long id;

    @NotNull
    @Min(value = 0, message = "parentId must not be negative")
    @Schema(description = "新的父栏目 ID，0 表示顶级栏目", example = "0")
    private Long parentId;

    @NotNull
    @Min(value = 0, message = "sortOrder must not be negative")
    @Schema(description = "新的排序值", example = "10")
    private Integer sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
