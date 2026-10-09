package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.common.api.Result;
import com.eems.dto.OperationLogPageQuery;
import com.eems.service.OperationLogService;
import com.eems.vo.OperationLogVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/logs")
@Tag(name = "操作日志", description = "管理员操作日志查询与审计追踪")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminOperationLogController {
    private final OperationLogService service;

    public AdminOperationLogController(OperationLogService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "分页查询操作日志", description = "支持操作人、模块、操作类型、结果和时间范围筛选")
    public Result<PageResult<OperationLogVO>> page(@Valid @ModelAttribute OperationLogPageQuery query) {
        return Result.success(service.page(query));
    }
}
