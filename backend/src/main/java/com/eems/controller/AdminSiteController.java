package com.eems.controller;

import com.eems.common.api.Result;
import com.eems.dto.SiteConfigUpdateRequest;
import com.eems.service.SiteConfigService;
import com.eems.vo.SiteConfigVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/site")
@Tag(name = "站点配置", description = "默认站点配置管理")
@SecurityRequirement(name = "bearerAuth")
public class AdminSiteController {
    private final SiteConfigService service;

    public AdminSiteController(SiteConfigService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "获取站点配置")
    public Result<SiteConfigVO> get() {
        return Result.success(service.getDefault());
    }

    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "更新站点配置")
    public Result<SiteConfigVO> update(@Valid @RequestBody SiteConfigUpdateRequest request,
                                       Authentication authentication) {
        return Result.success(service.updateDefault(request,
                authentication == null ? null : authentication.getName()));
    }
}
