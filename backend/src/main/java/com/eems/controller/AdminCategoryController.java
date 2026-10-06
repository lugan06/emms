package com.eems.controller;

import com.eems.common.api.Result;
import com.eems.dto.CategoryReorderRequest;
import com.eems.dto.CategorySaveRequest;
import com.eems.service.CategoryService;
import com.eems.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@Tag(name = "栏目管理", description = "栏目树维护、启停、导航显示和排序")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoryController {
    private final CategoryService service;

    public AdminCategoryController(CategoryService service) {
        this.service = service;
    }

    @GetMapping("/tree")
    @Operation(summary = "获取栏目树")
    public Result<List<CategoryVO>> tree() {
        return Result.success(service.tree());
    }

    @PostMapping
    @Operation(summary = "新增栏目")
    public Result<CategoryVO> create(@Valid @RequestBody CategorySaveRequest request) {
        return Result.success(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑栏目")
    public Result<CategoryVO> update(
            @Parameter(description = "栏目 ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody CategorySaveRequest request) {
        return Result.success(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除栏目")
    public Result<CategoryVO> delete(@Parameter(description = "栏目 ID", example = "1") @PathVariable Long id) {
        return Result.success(service.delete(id));
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复已删除栏目")
    public Result<CategoryVO> restore(
            @Parameter(description = "栏目 ID", example = "4") @PathVariable Long id) {
        return Result.success(service.restore(id));
    }

    @PostMapping("/reorder")
    @Operation(summary = "批量调整栏目排序和层级")
    public Result<Void> reorder(@Valid @RequestBody CategoryReorderRequest request) {
        service.reorder(request);
        return Result.success();
    }
}
