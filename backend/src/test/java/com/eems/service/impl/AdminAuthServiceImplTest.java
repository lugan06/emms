package com.eems.service.impl;

import com.eems.common.exception.BusinessException;
import com.eems.dto.AdminLoginRequest;
import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import com.eems.security.JwtProperties;
import com.eems.security.JwtTokenUtil;
import com.eems.vo.AdminLoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceImplTest {

    @Mock
    private AdminUserMapper adminUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    private AdminAuthServiceImpl adminAuthService;
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setAccessTokenTtl(7200);
        adminAuthService = new AdminAuthServiceImpl(
                adminUserMapper,
                passwordEncoder,
                jwtTokenUtil,
                jwtProperties);
    }

    @Test
    void loginShouldVerifyPasswordUpdateLoginInfoAndReturnToken() {
        AdminUser adminUser = enabledAdmin();
        when(adminUserMapper.selectOne(any())).thenReturn(adminUser);
        when(passwordEncoder.matches("plain-password", "bcrypt-hash")).thenReturn(true);
        when(jwtTokenUtil.generateToken(1L, "admin", "SUPER_ADMIN")).thenReturn("jwt-token");

        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("admin");
        request.setPassword("plain-password");

        AdminLoginResponse response = adminAuthService.login(request, "127.0.0.1");

        assertEquals("jwt-token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(7200, response.getExpiresIn());
        assertEquals("admin", response.getUser().getUsername());
        assertNotNull(adminUser.getLastLoginAt());
        assertEquals("127.0.0.1", adminUser.getLastLoginIp());
        verify(adminUserMapper).updateById(adminUser);
    }

    @Test
    void loginShouldRejectUnknownUser() {
        when(adminUserMapper.selectOne(any())).thenReturn(null);

        AdminLoginRequest request = loginRequest("not-found", "wrong-password");

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminAuthService.login(request, "127.0.0.1"));

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(adminUserMapper, never()).updateById(any(AdminUser.class));
    }

    @Test
    void loginShouldRejectWrongPassword() {
        AdminUser adminUser = enabledAdmin();
        when(adminUserMapper.selectOne(any())).thenReturn(adminUser);
        when(passwordEncoder.matches("wrong-password", "bcrypt-hash")).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminAuthService.login(loginRequest("admin", "wrong-password"), "127.0.0.1"));

        assertEquals("INVALID_CREDENTIALS", exception.getCode());
        verify(adminUserMapper, never()).updateById(any(AdminUser.class));
    }

    private AdminUser enabledAdmin() {
        AdminUser adminUser = new AdminUser();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setPasswordHash("bcrypt-hash");
        adminUser.setNickname("Administrator");
        adminUser.setRoleCode("SUPER_ADMIN");
        adminUser.setStatus("ENABLED");
        return adminUser;
    }

    private AdminLoginRequest loginRequest(String username, String password) {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }
}
