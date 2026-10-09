package com.eems.controller;

import com.eems.common.api.Result;
import com.eems.dto.CompanyProfileUpdateRequest;
import com.eems.service.CompanyProfileService;
import com.eems.vo.CompanyProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/company")
@Tag(name = "公司信息", description = "默认公司信息管理")
@SecurityRequirement(name = "bearerAuth")
public class AdminCompanyController {
    private final CompanyProfileService service;

    public AdminCompanyController(CompanyProfileService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "获取公司信息")
    public Result<CompanyProfileVO> get() {
        return Result.success(service.getDefault());
    }

    @PutMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "更新公司信息")
    public Result<CompanyProfileVO> update(@Valid @RequestBody CompanyProfileUpdateRequest request,
                                           Authentication authentication) {
        return Result.success(service.updateDefault(request,
                authentication == null ? null : authentication.getName()));
    }
}
