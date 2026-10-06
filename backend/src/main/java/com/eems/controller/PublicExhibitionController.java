package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.common.api.Result;
import com.eems.dto.PublicExhibitionPageQuery;
import com.eems.service.ExhibitionService;
import com.eems.vo.PublicExhibitionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/exhibitions")
@Tag(name = "公共展会", description = "公共浏览端展会列表和详情")
public class PublicExhibitionController {
    private final ExhibitionService service;

    public PublicExhibitionController(ExhibitionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "获取公开展会列表", description = "只返回已发布、未删除且已到发布时间的展会")
    public Result<PageResult<PublicExhibitionVO>> page(@Valid @ModelAttribute PublicExhibitionPageQuery query) {
        return Result.success(service.publicPage(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取公开展会详情", description = "不可公开的展会不会返回管理端数据")
    public Result<PublicExhibitionVO> get(
            @Parameter(description = "展会 ID", example = "1") @PathVariable Long id) {
        return Result.success(service.publicGet(id));
    }
}
