package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.common.api.Result;
import com.eems.dto.ContentPageQuery;
import com.eems.dto.ContentSaveRequest;
import com.eems.service.ContentService;
import com.eems.vo.ContentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/contents")
@Tag(name = "栏目内容管理", description = "栏目文章和页面内容维护、发布和下架")
@SecurityRequirement(name = "bearerAuth")
public class AdminContentController {
    private final ContentService service;

    public AdminContentController(ContentService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "分页查询栏目内容")
    public Result<PageResult<ContentVO>> page(@Valid @ModelAttribute ContentPageQuery query) {
        return Result.success(service.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取栏目内容详情")
    public Result<ContentVO> get(@Parameter(description = "内容 ID", example = "1") @PathVariable Long id) {
        return Result.success(service.get(id));
    }

    @PostMapping
    @Operation(summary = "新增栏目内容")
    public Result<ContentVO> create(@Valid @RequestBody ContentSaveRequest request,
                                    Authentication authentication) {
        return Result.success(service.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑栏目内容")
    public Result<ContentVO> update(@Parameter(description = "内容 ID", example = "1") @PathVariable Long id,
                                    @Valid @RequestBody ContentSaveRequest request,
                                    Authentication authentication) {
        return Result.success(service.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除栏目内容")
    public Result<Void> delete(@Parameter(description = "内容 ID", example = "1") @PathVariable Long id,
                               Authentication authentication) {
        service.delete(id, authentication.getName());
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布栏目内容")
    public Result<ContentVO> publish(@PathVariable Long id, Authentication authentication) {
        return Result.success(service.publish(id, authentication.getName()));
    }

    @PostMapping("/{id}/offline")
    @Operation(summary = "下架栏目内容")
    public Result<ContentVO> offline(@PathVariable Long id, Authentication authentication) {
        return Result.success(service.offline(id, authentication.getName()));
    }
}
