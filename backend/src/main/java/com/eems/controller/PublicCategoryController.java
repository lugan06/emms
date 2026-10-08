package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.common.api.Result;
import com.eems.dto.PublicContentPageQuery;
import com.eems.service.PublicContentService;
import com.eems.vo.PublicCategoryVO;
import com.eems.vo.PublicContentListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
@Tag(name = "公共栏目", description = "公共端导航栏目和栏目内容")
public class PublicCategoryController {
    private final PublicContentService service;

    public PublicCategoryController(PublicContentService service) {
        this.service = service;
    }

    @GetMapping("/tree")
    @Operation(summary = "获取公共栏目树", description = "只返回启用且显示在导航中的栏目")
    public Result<List<PublicCategoryVO>> tree() {
        return Result.success(service.categoryTree());
    }

    @GetMapping("/{code}/contents")
    @Operation(summary = "获取栏目公开内容", description = "只返回已发布、已到发布时间且未删除的内容")
    public Result<PageResult<PublicContentListVO>> contents(
            @Parameter(description = "栏目编码", example = "exhibition-news") @PathVariable String code,
            @Valid @ModelAttribute PublicContentPageQuery query) {
        return Result.success(service.pageByCategory(code, query));
    }
}
