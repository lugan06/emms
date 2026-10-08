package com.eems.controller;

import com.eems.common.api.Result;
import com.eems.service.PublicContentService;
import com.eems.vo.PublicContentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/contents")
@Tag(name = "公共内容", description = "公共端内容详情")
public class PublicContentController {
    private final PublicContentService service;

    public PublicContentController(PublicContentService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取公开内容详情", description = "不可公开的内容不会返回管理端数据")
    public Result<PublicContentVO> get(
            @Parameter(description = "内容 ID", example = "1") @PathVariable Long id) {
        return Result.success(service.get(id));
    }
}
