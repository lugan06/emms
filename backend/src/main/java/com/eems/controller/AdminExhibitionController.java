package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.common.api.Result;
import com.eems.dto.ExhibitionPageQuery;
import com.eems.dto.ExhibitionSaveRequest;
import com.eems.service.ExhibitionService;
import com.eems.vo.ExhibitionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/admin/exhibitions")
@Tag(name = "展会管理", description = "展会信息维护、发布和当前展会设置")
@SecurityRequirement(name = "bearerAuth")
public class AdminExhibitionController {
    private final ExhibitionService service;

    public AdminExhibitionController(ExhibitionService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "分页查询展会")
    public Result<PageResult<ExhibitionVO>> page(@Valid @ModelAttribute ExhibitionPageQuery query) {
        return Result.success(service.page(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取展会详情")
    public Result<ExhibitionVO> get(@Parameter(description = "展会 ID", example = "1") @PathVariable Long id) {
        return Result.success(service.get(id));
    }

    @PostMapping
    @Operation(summary = "新增展会")
    public Result<ExhibitionVO> create(@Valid @RequestBody ExhibitionSaveRequest request,
                                       Authentication authentication) {
        return Result.success(service.create(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑展会")
    public Result<ExhibitionVO> update(@PathVariable Long id, @Valid @RequestBody ExhibitionSaveRequest request,
                                       Authentication authentication) {
        return Result.success(service.update(id, request, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "逻辑删除展会")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(id, authentication.getName());
        return Result.success();
    }

    @PostMapping("/{id}/restore")
    @Operation(summary = "恢复已删除展会")
    public Result<ExhibitionVO> restore(@Parameter(description = "展会 ID", example = "1") @PathVariable Long id,
                                        Authentication authentication) {
        return Result.success(service.restore(id, authentication.getName()));
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布展会")
    public Result<ExhibitionVO> publish(@PathVariable Long id, Authentication authentication) {
        return Result.success(service.publish(id, authentication.getName()));
    }

    @PostMapping("/{id}/republish")
    @Operation(summary = "重新上架展会")
    public Result<ExhibitionVO> republish(@Parameter(description = "展会 ID", example = "1") @PathVariable Long id,
                                          Authentication authentication) {
        return Result.success(service.republish(id, authentication.getName()));
    }

    @PostMapping("/{id}/offline")
    @Operation(summary = "下架展会")
    public Result<ExhibitionVO> offline(@PathVariable Long id, Authentication authentication) {
        return Result.success(service.offline(id, authentication.getName()));
    }

    @PostMapping("/{id}/set-current")
    @Operation(summary = "设置当前展会")
    public Result<ExhibitionVO> setCurrent(@PathVariable Long id, Authentication authentication) {
        return Result.success(service.setCurrent(id, authentication.getName()));
    }
}
