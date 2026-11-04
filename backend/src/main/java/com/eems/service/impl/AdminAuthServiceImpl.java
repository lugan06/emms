package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eems.common.exception.BusinessException;
import com.eems.dto.AdminLoginRequest;
import com.eems.dto.AdminPasswordChangeRequest;
import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.AdminUserSiteMapper;
import com.eems.security.JwtProperties;
import com.eems.security.JwtTokenUtil;
import com.eems.service.AdminAuthService;
import com.eems.service.OperationLogService;
import com.eems.site.SiteContext;
import com.eems.site.SiteResolutionException;
import com.eems.site.SiteResolver;
import com.eems.vo.AdminLoginResponse;
import com.eems.vo.AdminUserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {
    private final AdminUserMapper users;
    private final AdminUserSiteMapper bindings;
    private final PasswordEncoder passwords;
    private final JwtTokenUtil tokens;
    private final JwtProperties jwtProperties;
    private final OperationLogService logs;
    private final SiteResolver sites;

    public AdminAuthServiceImpl(AdminUserMapper users, AdminUserSiteMapper bindings,
                                PasswordEncoder passwords, JwtTokenUtil tokens, JwtProperties jwtProperties,
                                OperationLogService logs, SiteResolver sites) {
        this.users = users;
        this.bindings = bindings;
        this.passwords = passwords;
        this.tokens = tokens;
        this.jwtProperties = jwtProperties;
        this.logs = logs;
        this.sites = sites;
    }

    @Override
    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request, String clientIp) {
        Long siteId = SiteContext.requireSiteId();
        AdminUser user = users.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, request.getUsername()).eq(AdminUser::getStatus, "ENABLED"));
        if (user == null || !passwords.matches(request.getPassword(), user.getPasswordHash())) {
            logs.failure("AUTH", request.getUsername(), "登录", "POST", "/api/admin/login",
                    clientIp, "{}", "INVALID_CREDENTIALS");
            throw new BusinessException("INVALID_CREDENTIALS", "账号或密码错误");
        }
        try {
            SiteResolver.requireEnabled(sites.requireSite(siteId));
            if (!("SUPER_ADMIN".equals(user.getRoleCode()) || ("EDITOR".equals(user.getRoleCode())
                    && bindings.existsBinding(user.getId(), siteId)))) {
                throw new SiteResolutionException("SITE_ACCESS_DENIED", "没有当前站点的管理权限", 403);
            }
        } catch (SiteResolutionException exception) {
            logs.failure("AUTH", user.getUsername(), "登录", "POST", "/api/admin/login",
                    clientIp, "{}", exception.getCode());
            throw exception;
        }
        // Update only login metadata, never overwrite a concurrent password/version/status change.
        LocalDateTime loginAt = LocalDateTime.now();
        if (users.update(null, Wrappers.<AdminUser>lambdaUpdate()
                .eq(AdminUser::getId, user.getId()).eq(AdminUser::getStatus, "ENABLED")
                .eq(AdminUser::getAuthVersion, user.getAuthVersion())
                .set(AdminUser::getLastLoginAt, loginAt).set(AdminUser::getLastLoginIp, clientIp)) != 1) {
            throw new BusinessException("LOGIN_STATE_CHANGED", "账号状态已变更，请重新登录");
        }
        String token = tokens.generateToken(user.getId(), user.getUsername(), user.getRoleCode(),
                siteId, user.getAuthVersion());
        logs.success("AUTH", user.getUsername(), "登录", "POST", "/api/admin/login", clientIp, "{}");
        return new AdminLoginResponse(token, "Bearer", jwtProperties.getAccessTokenTtl(), toUserVO(user));
    }

    @Override
    public AdminUserVO currentUser(String username) {
        return toUserVO(findEnabledUser(username));
    }

    @Override
    @Transactional
    public void changePassword(String username, AdminPasswordChangeRequest request) {
        AdminUser user = findEnabledUser(username);
        if (!passwords.matches(request.oldPassword(), user.getPasswordHash())) {
            logs.failure("AUTH", username, "修改密码", "PUT", "/api/admin/password", null, "{}", "INVALID_PASSWORD");
            throw new BusinessException("INVALID_PASSWORD", "原密码错误");
        }
        long nextVersion = Math.incrementExact(user.getAuthVersion());
        String newHash = passwords.encode(request.newPassword());
        if (users.update(null, Wrappers.<AdminUser>lambdaUpdate()
                .eq(AdminUser::getId, user.getId()).eq(AdminUser::getStatus, "ENABLED")
                .eq(AdminUser::getAuthVersion, user.getAuthVersion())
                .eq(AdminUser::getPasswordHash, user.getPasswordHash())
                .set(AdminUser::getPasswordHash, newHash).set(AdminUser::getAuthVersion, nextVersion)) != 1) {
            throw new BusinessException("PASSWORD_STATE_CHANGED", "账号状态已变更，请重新登录后重试");
        }
        logs.success("AUTH", username, "修改密码", "PUT", "/api/admin/password", null, "{}");
    }

    private AdminUser findEnabledUser(String username) {
        AdminUser user = users.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username).eq(AdminUser::getStatus, "ENABLED"));
        if (user == null) {
            throw new BusinessException("ADMIN_NOT_FOUND", "管理员不存在或已禁用");
        }
        return user;
    }

    private AdminUserVO toUserVO(AdminUser user) {
        return new AdminUserVO(user.getId(), user.getUsername(), user.getNickname(), user.getAvatarUrl(), user.getRoleCode());
    }
}
