package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eems.common.exception.BusinessException;
import com.eems.dto.AdminLoginRequest;
import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import com.eems.security.JwtProperties;
import com.eems.security.JwtTokenUtil;
import com.eems.service.AdminAuthService;
import com.eems.vo.AdminLoginResponse;
import com.eems.vo.AdminUserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final String ENABLED = "ENABLED";

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtProperties jwtProperties;

    public AdminAuthServiceImpl(AdminUserMapper adminUserMapper,
                                PasswordEncoder passwordEncoder,
                                JwtTokenUtil jwtTokenUtil,
                                JwtProperties jwtProperties) {
        this.adminUserMapper = adminUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.jwtProperties = jwtProperties;
    }

    @Override
    @Transactional
    public AdminLoginResponse login(AdminLoginRequest request, String clientIp) {
        AdminUser adminUser = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, request.getUsername())
                .eq(AdminUser::getStatus, ENABLED));

        if (adminUser == null || !passwordEncoder.matches(request.getPassword(), adminUser.getPasswordHash())) {
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
        return new AdminLoginResponse(token, "Bearer", jwtProperties.getAccessTokenTtl(), user);
    }
}
