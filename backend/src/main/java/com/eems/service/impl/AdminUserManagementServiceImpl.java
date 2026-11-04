package com.eems.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.api.PageResult;
import com.eems.common.exception.BusinessException;
import com.eems.dto.*;
import com.eems.entity.*;
import com.eems.mapper.*;
import com.eems.service.*;
import com.eems.site.SiteResolutionException;
import com.eems.vo.*;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminUserManagementServiceImpl implements AdminUserManagementService {
    private final AdminUserMapper users;
    private final AdminUserSiteMapper bindings;
    private final SiteManagementMapper sites;
    private final PasswordEncoder passwords;
    private final OperationLogService logs;

    public AdminUserManagementServiceImpl(AdminUserMapper users, AdminUserSiteMapper bindings,
            SiteManagementMapper sites, PasswordEncoder passwords, OperationLogService logs) {
        this.users = users;
        this.bindings = bindings;
        this.sites = sites;
        this.passwords = passwords;
        this.logs = logs;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ManagedAdminUserVO> page(AdminUserPageQuery query) {
        var wrapper = Wrappers.<AdminUser>lambdaQuery().eq(AdminUser::getRoleCode, "EDITOR");
        if (query.getStatus() != null) wrapper.eq(AdminUser::getStatus, query.getStatus());
        if (query.getSiteId() != null) wrapper.apply(
                "id IN (SELECT admin_user_id FROM admin_user_site WHERE site_id = {0})", query.getSiteId());
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = "%" + query.getKeyword().trim().toLowerCase(Locale.ROOT)
                    .replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
            wrapper.apply("(LOWER(username) LIKE {0} ESCAPE '!' OR LOWER(nickname) LIKE {0} ESCAPE '!')", keyword);
        }
        wrapper.orderByDesc(AdminUser::getCreatedAt).orderByDesc(AdminUser::getId);
        var page = users.selectPage(new Page<AdminUser>(query.getPage(), query.getPageSize()), wrapper);
        List<AdminUser> records = page.getRecords();
        if (records.isEmpty()) return new PageResult<>(List.of(), page.getTotal(), page.getCurrent(), page.getSize());
        var grants = bindings.selectByAdminUserIds(records.stream().map(AdminUser::getId).toList()).stream()
                .collect(Collectors.groupingBy(AdminUserSite::getAdminUserId,
                        Collectors.mapping(AdminUserSite::getSiteId, Collectors.toList())));
        var siteMap = siteOptions(grants.values().stream().flatMap(Collection::stream).distinct().toList());
        return new PageResult<>(records.stream().map(u -> toVO(u, grants.getOrDefault(u.getId(), List.of()), siteMap)).toList(),
                page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public ManagedAdminUserVO create(AdminUserCreateRequest request, String operator) {
        validatePassword(request.password());
        List<Long> siteIds = validateSites(request.siteIds());
        AdminUser user = new AdminUser();
        user.setUsername(request.username());
        user.setNickname(request.nickname().trim());
        user.setPasswordHash(passwords.encode(request.password()));
        user.setRoleCode("EDITOR");
        user.setStatus("ENABLED");
        user.setAuthVersion(0L);
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(user.getCreatedAt());
        try {
            users.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("ADMIN_USERNAME_EXISTS", "用户名已存在");
        }
        replaceBindings(user.getId(), siteIds);
        audit(user.getId(), operator, "创建管理员", "POST", "/api/admin/users", siteIds);
        return toVO(user, siteIds, siteOptions(siteIds));
    }

    @Override
    @Transactional
    public ManagedAdminUserVO update(Long id, AdminUserUpdateRequest request, String operator) {
        AdminUser user = requireEditor(id);
        String nickname = request.nickname().trim();
        checkedUpdate(user, Wrappers.<AdminUser>lambdaUpdate().set(AdminUser::getNickname, nickname));
        user.setNickname(nickname);
        audit(id, operator, "修改管理员", "PUT", "/api/admin/users/" + id, null);
        return detail(user);
    }

    @Override
    @Transactional
    public ManagedAdminUserVO assignSites(Long id, List<Long> input, String operator) {
        AdminUser user = requireEditor(id);
        List<Long> siteIds = validateSites(input);
        // Hold the account row lock throughout delete/insert/version/audit so assignments cannot interleave.
        replaceBindings(id, siteIds);
        bumpVersion(user, Wrappers.<AdminUser>lambdaUpdate());
        audit(id, operator, "分配站点授权", "PUT", "/api/admin/users/" + id + "/sites", siteIds);
        return toVO(user, siteIds, siteOptions(siteIds));
    }

    @Override
    @Transactional
    public ManagedAdminUserVO changeStatus(Long id, String status, String operator) {
        AdminUser user = requireEditor(id);
        if (!"ENABLED".equals(status) && !"DISABLED".equals(status)) {
            throw new BusinessException("INVALID_ADMIN_STATUS", "无效的账号状态");
        }
        if (!status.equals(user.getStatus())) {
            bumpVersion(user, Wrappers.<AdminUser>lambdaUpdate().set(AdminUser::getStatus, status));
            user.setStatus(status);
        }
        audit(id, operator, "ENABLED".equals(status) ? "启用管理员" : "禁用管理员",
                "PUT", "/api/admin/users/" + id + "/status", null);
        return detail(user);
    }

    @Override
    @Transactional
    public void resetPassword(Long id, String password, String operator) {
        AdminUser user = requireEditor(id);
        validatePassword(password);
        bumpVersion(user, Wrappers.<AdminUser>lambdaUpdate()
                .set(AdminUser::getPasswordHash, passwords.encode(password)));
        audit(id, operator, "重置管理员密码", "PUT", "/api/admin/users/" + id + "/password", null);
    }

    private AdminUser requireEditor(Long id) {
        AdminUser user = users.lockEditor(id);
        if (user == null || !"EDITOR".equals(user.getRoleCode()) || !Integer.valueOf(0).equals(user.getDeleted())) {
            throw new SiteResolutionException("EDITOR_NOT_FOUND", "普通管理员不存在", 404);
        }
        return user;
    }

    private void bumpVersion(AdminUser user, com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AdminUser> update) {
        long next;
        try { next = Math.incrementExact(user.getAuthVersion()); }
        catch (ArithmeticException exception) { throw new BusinessException("AUTH_VERSION_EXHAUSTED", "认证版本已达上限"); }
        checkedUpdate(user, update.set(AdminUser::getAuthVersion, next));
        user.setAuthVersion(next);
    }

    private void checkedUpdate(AdminUser user, com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<AdminUser> update) {
        LocalDateTime updatedAt = LocalDateTime.now();
        int changed = users.update(null, update.eq(AdminUser::getId, user.getId())
                .eq(AdminUser::getRoleCode, "EDITOR").eq(AdminUser::getAuthVersion, user.getAuthVersion())
                .set(AdminUser::getUpdatedAt, updatedAt));
        if (changed != 1) throw new BusinessException("ADMIN_STATE_CHANGED", "账号状态已变化，请刷新后重试");
        user.setUpdatedAt(updatedAt);
    }

    private List<Long> validateSites(List<Long> input) {
        if (input == null || input.size() > 1000 || input.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BusinessException("INVALID_SITE_IDS", "请提供有效的站点 ID 列表");
        }
        var ids = input.stream().distinct().sorted().toList();
        if (ids.size() != input.size()) throw new BusinessException("DUPLICATE_SITE_IDS", "站点 ID 不能重复");
        if (!ids.isEmpty() && sites.selectCount(Wrappers.<Site>lambdaQuery().in(Site::getId, ids)) != ids.size()) {
            throw new BusinessException("SITE_NOT_FOUND", "存在未找到的授权站点");
        }
        return ids;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank() || password.length() < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new BusinessException("INVALID_ADMIN_PASSWORD", "密码至少 8 字符且 UTF-8 不超过 72 字节");
        }
    }

    private void replaceBindings(Long id, List<Long> siteIds) {
        bindings.deleteByAdminUserId(id);
        for (Long siteId : siteIds) bindings.insertBinding(id, siteId);
    }

    private ManagedAdminUserVO detail(AdminUser user) {
        List<Long> ids = bindings.selectByAdminUserId(user.getId()).stream().map(AdminUserSite::getSiteId).toList();
        return toVO(user, ids, siteOptions(ids));
    }

    private Map<Long, SiteOptionVO> siteOptions(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return sites.selectList(Wrappers.<Site>lambdaQuery().in(Site::getId, ids)).stream()
                .collect(Collectors.toMap(Site::getId, SiteOptionVO::from));
    }

    private ManagedAdminUserVO toVO(AdminUser user, List<Long> ids, Map<Long, SiteOptionVO> options) {
        return new ManagedAdminUserVO(user.getId(), user.getUsername(), user.getNickname(), user.getStatus(), ids,
                ids.stream().map(options::get).filter(Objects::nonNull).toList(), user.getCreatedAt(), user.getUpdatedAt());
    }

    private void audit(Long id, String operator, String action, String method, String url, List<Long> siteIds) {
        String metadata = "{\"adminUserId\":" + id + (siteIds == null ? "" : ",\"siteIds\":" + siteIds) + "}";
        logs.success("ADMIN_USER", operator, action, method, url, null, metadata);
    }
}

