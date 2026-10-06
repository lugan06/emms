package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "栏目批量排序和移动参数")
public class CategoryReorderRequest {
    @NotEmpty
    @Valid
    @Schema(description = "栏目排序项列表")
    private List<CategoryReorderItem> items;

    public List<CategoryReorderItem> getItems() { return items; }
    public void setItems(List<CategoryReorderItem> items) { this.items = items; }
}
