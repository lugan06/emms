package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eems.common.exception.BusinessException;
import com.eems.dto.AdminLoginRequest;
import com.eems.dto.AdminPasswordChangeRequest;
import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import com.eems.security.JwtProperties;
import com.eems.security.JwtTokenUtil;
import com.eems.service.AdminAuthService;
import com.eems.service.OperationLogService;
import com.eems.vo.AdminLoginResponse;
import com.eems.vo.AdminUserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final String ENABLED = "ENABLED";

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtProperties jwtProperties;
    private final OperationLogService operationLogService;

    public AdminAuthServiceImpl(AdminUserMapper adminUserMapper,
                                PasswordEncoder passwordEncoder,
                                JwtTokenUtil jwtTokenUtil,
                                JwtProperties jwtProperties) {
        this(adminUserMapper, passwordEncoder, jwtTokenUtil, jwtProperties, null);
    }

    @Autowired
    public AdminAuthServiceImpl(AdminUserMapper adminUserMapper,
                                PasswordEncoder passwordEncoder,
                                JwtTokenUtil jwtTokenUtil,
                                JwtProperties jwtProperties,
                                OperationLogService operationLogService) {
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtProperties = jwtProperties;
        this.operationLogService = operationLogService;
    }

    @Override
    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request, String clientIp) {
        AdminUser adminUser = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, request.getUsername())
                .eq(AdminUser::getStatus, ENABLED));

        if (adminUser == null || !passwordEncoder.matches(request.getPassword(), adminUser.getPasswordHash())) {
            if (operationLogService != null) {
                operationLogService.failure("AUTH", request.getUsername(), "登录", "POST", "/api/admin/login",
                        clientIp, "{\"username\":\"" + request.getUsername() + "\"}", "账号或密码错误");
            }
            throw new BusinessException("INVALID_CREDENTIALS", "账号或密码错误");
        }

        LocalDateTime loginAt = LocalDateTime.now();
        adminUser.setLastLoginAt(loginAt);
        adminUser.setLastLoginIp(clientIp);
        adminUserMapper.updateById(adminUser);

        String token = jwtTokenUtil.generateToken(adminUser.getId(), adminUser.getUsername(), adminUser.getRoleCode());
        AdminUserVO user = new AdminUserVO(
                adminUser.getId(),
                adminUser.getUsername(),
                adminUser.getNickname(),
                adminUser.getAvatarUrl(),
                adminUser.getRoleCode());
        if (operationLogService != null) {
            operationLogService.success("AUTH", adminUser.getUsername(), "登录", "POST", "/api/admin/login",
                    clientIp, "{\"username\":\"" + adminUser.getUsername() + "\"}");
        }
        return new AdminLoginResponse(token, "Bearer", jwtProperties.getAccessTokenTtl(), user);
    }

    @Override
    public AdminUserVO currentUser(String username) {
        AdminUser adminUser = findEnabledUser(username);
        return toUserVO(adminUser);
    }

    @Override
    @Transactional
    public void changePassword(String username, AdminPasswordChangeRequest request) {
        AdminUser adminUser = findEnabledUser(username);
        if (!passwordEncoder.matches(request.oldPassword(), adminUser.getPasswordHash())) {
            if (operationLogService != null) {
                operationLogService.failure("AUTH", username, "修改密码", "PUT", "/api/admin/password",
                        null, "{}", "原密码错误");
            }
            throw new BusinessException("INVALID_PASSWORD", "原密码错误");
        }
        adminUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        adminUserMapper.updateById(adminUser);
        if (operationLogService != null) {
            operationLogService.success("AUTH", username, "修改密码", "PUT", "/api/admin/password",
                    null, "{}");
        }
    }

    private AdminUser findEnabledUser(String username) {
        AdminUser adminUser = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)
                .eq(AdminUser::getStatus, ENABLED));
        if (adminUser == null) {
            throw new BusinessException("ADMIN_NOT_FOUND", "管理员不存在或已禁用");
        }
        return adminUser;
    }

    private AdminUserVO toUserVO(AdminUser adminUser) {
        return new AdminUserVO(adminUser.getId(), adminUser.getUsername(), adminUser.getNickname(),
                adminUser.getAvatarUrl(), adminUser.getRoleCode());
    }
}
