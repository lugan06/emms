package com.eems.controller;

import com.eems.common.api.*;
import com.eems.dto.*;
import com.eems.service.AdminUserManagementService;
import com.eems.vo.ManagedAdminUserVO;
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
@RequestMapping("/api/admin/users")
@Tag(name = "管理员管理", description = "仅 SUPER_ADMIN 可管理 EDITOR 账号及站点授权，不接受 X-Site-Id")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@Validated
public class AdminUserManagementController {
    private final AdminUserManagementService service;
    public AdminUserManagementController(AdminUserManagementService service) { this.service = service; }

    @GetMapping
    @Operation(summary = "分页查询普通管理员", description = "支持 keyword（用户名/昵称）、status、siteId；排除超管和已删除账号")
    public Result<PageResult<ManagedAdminUserVO>> page(@Valid @ParameterObject AdminUserPageQuery query) {
        return Result.success(service.page(query));
    }

    @PostMapping
    @Operation(summary = "创建普通管理员", description = "角色固定 EDITOR，初始启用；账号、站点授权、审计记录同一事务")
    public Result<ManagedAdminUserVO> create(@Valid @RequestBody AdminUserCreateRequest request, Authentication auth) {
        return Result.success(service.create(request, auth.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新普通管理员昵称", description = "不可修改用户名或角色；超管及已删除账号返回 404")
    public Result<ManagedAdminUserVO> update(@PathVariable @Positive Long id,
            @Valid @RequestBody AdminUserUpdateRequest request, Authentication auth) {
        return Result.success(service.update(id, request, auth.getName()));
    }

    @PutMapping("/{id}/sites")
    @Operation(summary = "重新分配授权站点", description = "完整替换授权，允许空数组；事务内递增认证版本，旧 Token 失效")
    public Result<ManagedAdminUserVO> sites(@PathVariable @Positive Long id,
            @Valid @RequestBody AdminUserSitesRequest request, Authentication auth) {
        return Result.success(service.assignSites(id, request.siteIds(), auth.getName()));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "启用或禁用普通管理员", description = "状态变更递增 auth_version；重新启用不会恢复旧 Token")
    public Result<ManagedAdminUserVO> status(@PathVariable @Positive Long id,
            @Valid @RequestBody AdminUserStatusRequest request, Authentication auth) {
        return Result.success(service.changeStatus(id, request.status(), auth.getName()));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "重置普通管理员密码", description = "使用 BCrypt 保存并递增 auth_version，所有旧 Token 立即失效")
    public Result<Void> password(@PathVariable @Positive Long id,
            @Valid @RequestBody AdminUserResetPasswordRequest request, Authentication auth) {
        service.resetPassword(id, request.password(), auth.getName());
        return Result.success();
    }
}

