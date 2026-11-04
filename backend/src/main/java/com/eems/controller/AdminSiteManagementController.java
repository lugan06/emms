package com.eems.controller;

import com.eems.common.api.*;
import com.eems.dto.*;
import com.eems.service.SiteManagementService;
import com.eems.vo.ManagedSiteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sites")
@Tag(name = "站点管理", description = "全局站点及域名管理，仅 SUPER_ADMIN，不接受 X-Site-Id")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Validated
public class AdminSiteManagementController {
    private final SiteManagementService service;

    public AdminSiteManagementController(SiteManagementService service) { this.service = service; }

    @GetMapping("/options")
    @Operation(summary = "查询可授权站点选项", description = "返回全部站点的 ID、名称、编码和状态，包含停用站点，仅 SUPER_ADMIN")
    public Result<java.util.List<com.eems.vo.SiteOptionVO>> options() {
        return Result.success(service.options());
    }

    @GetMapping
    @Operation(summary = "分页查询站点", description = "支持名称、编码、绑定域名 keyword 搜索及 status 筛选，包含完整域名列表")
    public Result<PageResult<ManagedSiteVO>> page(@Valid @ParameterObject SitePageQuery query) {
        return Result.success(service.page(query));
    }

    @PostMapping
    @Operation(summary = "新建站点", description = "默认停用；同一事务内初始化域名、唯一 default 配置及操作日志")
    public Result<ManagedSiteVO> create(@Valid @RequestBody SiteSaveRequest request, Authentication auth) {
        return Result.success(service.create(request, auth.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新站点及域名", description = "编码不可修改；domains 完整替换，必须且只能有一个主域名")
    public Result<ManagedSiteVO> update(@PathVariable @Positive Long id,
                                        @Valid @RequestBody SiteSaveRequest request, Authentication auth) {
        return Result.success(service.update(id, request, auth.getName()));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启用或停用站点", description = "启用前确认主域名有效；停用后公共访问及新登录将被阻止")
    public Result<ManagedSiteVO> status(@PathVariable @Positive Long id,
                                        @Valid @RequestBody SiteStatusRequest request, Authentication auth) {
        return Result.success(service.changeStatus(id, request.status(), auth.getName()));
    }
}

