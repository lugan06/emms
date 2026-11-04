package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.eems.common.exception.BusinessException;
import com.eems.dto.AdminLoginRequest;
import com.eems.dto.AdminPasswordChangeRequest;
import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.AdminUserSiteMapper;
import com.eems.security.JwtProperties;
import com.eems.security.JwtTokenUtil;
import com.eems.service.OperationLogService;
import com.eems.site.SiteContextTestSupport;
import com.eems.site.SiteResolutionException;
import com.eems.site.SiteResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminAuthServiceImplTest {
    private final AdminUserMapper users = mock(AdminUserMapper.class);
    private final AdminUserSiteMapper bindings = mock(AdminUserSiteMapper.class);
    private final org.springframework.security.crypto.password.PasswordEncoder passwords =
            mock(org.springframework.security.crypto.password.PasswordEncoder.class);
    private final JwtTokenUtil tokens = mock(JwtTokenUtil.class);
    private final OperationLogService logs = mock(OperationLogService.class);
    private final SiteResolver sites = mock(SiteResolver.class);
    private AdminAuthServiceImpl service;

    @BeforeEach
    void setup() {
        JwtProperties properties = new JwtProperties();
        properties.setAccessTokenTtl(7200);
        service = new AdminAuthServiceImpl(users, bindings, passwords, tokens, properties, logs, sites);
        SiteContextTestSupport.selectSite(2L);
    }

    @AfterEach
    void clear() {
        SiteContextTestSupport.clear();
    }

    @Test
    void superAdminCanLoginNonDefaultSiteWithoutBinding() {
        setupSuccessfulCredentials("SUPER_ADMIN");
        when(tokens.generateToken(1L, "admin", "SUPER_ADMIN", 2L, 0L)).thenReturn("test-token");
        var result = service.login(login("plain-password"), "127.0.0.1");
        assertEquals("test-token", result.getToken());
        assertEquals(7200, result.getExpiresIn());
        assertEquals("admin", result.getUser().getUsername());
        verifyNoInteractions(bindings);
        verify(users, never()).updateById(any(AdminUser.class));
    }

    @Test
    void editorMustHaveCurrentSiteGrant() {
        setupSuccessfulCredentials("EDITOR");
        when(bindings.existsBinding(1L, 2L)).thenReturn(true);
        service.login(login("plain-password"), "127.0.0.1");
        verify(tokens).generateToken(1L, "admin", "EDITOR", 2L, 0L);
    }

    @Test
    void editorWithoutGrantIsDeniedBeforeIssuingTokenOrUpdatingLogin() {
        setupCredentials("EDITOR");
        when(sites.requireSite(2L)).thenReturn(new SiteResolver.ResolvedSite(2L, "ENABLED"));
        assertEquals("SITE_ACCESS_DENIED", assertThrows(SiteResolutionException.class,
                () -> service.login(login("plain-password"), "127.0.0.1")).getCode());
        verifyNoInteractions(tokens);
        verify(users, never()).update(any(), any(Wrapper.class));
        verify(logs).failure(eq("AUTH"), eq("admin"), eq("登录"), any(), any(), any(), eq("{}"), eq("SITE_ACCESS_DENIED"));
    }

    @Test
    void unknownRoleIsDenied() {
        setupCredentials("OTHER");
        when(sites.requireSite(2L)).thenReturn(new SiteResolver.ResolvedSite(2L, "ENABLED"));
        assertThrows(SiteResolutionException.class, () -> service.login(login("plain-password"), "127.0.0.1"));
        verifyNoInteractions(tokens, bindings);
    }

    @Test
    void evenSuperAdminCannotLoginDisabledSite() {
        setupCredentials("SUPER_ADMIN");
        when(sites.requireSite(2L)).thenReturn(new SiteResolver.ResolvedSite(2L, "DISABLED"));
        assertEquals("SITE_DISABLED", assertThrows(SiteResolutionException.class,
                () -> service.login(login("plain-password"), "127.0.0.1")).getCode());
        verifyNoInteractions(tokens);
    }

    @Test
    void unknownUserAndBadPasswordAreRejected() {
        assertEquals("INVALID_CREDENTIALS", assertThrows(BusinessException.class,
                () -> service.login(login("wrong"), "127.0.0.1")).getCode());
        when(users.selectOne(any())).thenReturn(user("SUPER_ADMIN"));
        assertEquals("INVALID_CREDENTIALS", assertThrows(BusinessException.class,
                () -> service.login(login("wrong"), "127.0.0.1")).getCode());
        verifyNoInteractions(tokens, sites);
    }

    @Test
    void loginFailsIfAccountChangedDuringPasswordVerification() {
        setupCredentials("SUPER_ADMIN");
        when(sites.requireSite(2L)).thenReturn(new SiteResolver.ResolvedSite(2L, "ENABLED"));
        assertEquals("LOGIN_STATE_CHANGED", assertThrows(BusinessException.class,
                () -> service.login(login("plain-password"), "127.0.0.1")).getCode());
        verifyNoInteractions(tokens);
    }

    @Test
    void currentUserReturnsDatabaseProfile() {
        when(users.selectOne(any())).thenReturn(user("EDITOR"));
        assertEquals("EDITOR", service.currentUser("admin").getRoleCode());
    }

    @Test
    void passwordAndVersionAreChangedAtomicallyWithOldStateConditions() {
        when(users.selectOne(any())).thenReturn(user("SUPER_ADMIN"));
        when(passwords.matches("old", "old-hash")).thenReturn(true);
        when(passwords.encode("new-password")).thenReturn("new-hash");
        when(users.update(isNull(), any(Wrapper.class))).thenReturn(1);
        service.changePassword("admin", new AdminPasswordChangeRequest("old", "new-password"));
        ArgumentCaptor<Wrapper<AdminUser>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(users).update(isNull(), captor.capture());
        var update = (LambdaUpdateWrapper<AdminUser>) captor.getValue();
        String where = update.getSqlSegment();
        assertTrue(where.contains("auth_version"));
        assertTrue(where.contains("password_hash"));
        assertTrue(where.contains("status"));
        assertTrue(update.getSqlSet().contains("auth_version"));
        assertTrue(update.getSqlSet().contains("password_hash"));
        Map<String, Object> params = update.getParamNameValuePairs();
        assertTrue(params.containsValue(0L));
        assertTrue(params.containsValue(1L));
        assertTrue(params.containsValue("old-hash"));
        assertTrue(params.containsValue("new-hash"));
    }

    @Test
    void failedConcurrentPasswordUpdateDoesNotReportSuccess() {
        when(users.selectOne(any())).thenReturn(user("SUPER_ADMIN"));
        when(passwords.matches("old", "old-hash")).thenReturn(true);
        when(passwords.encode("new-password")).thenReturn("new-hash");
        assertEquals("PASSWORD_STATE_CHANGED", assertThrows(BusinessException.class,
                () -> service.changePassword("admin", new AdminPasswordChangeRequest("old", "new-password"))).getCode());
        verify(logs, never()).success(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void wrongOldPasswordDoesNotWriteHashOrVersion() {
        when(users.selectOne(any())).thenReturn(user("SUPER_ADMIN"));
        assertEquals("INVALID_PASSWORD", assertThrows(BusinessException.class,
                () -> service.changePassword("admin", new AdminPasswordChangeRequest("wrong", "new-password"))).getCode());
        verifyNoInteractions(tokens);
        verify(users, never()).update(any(), any(Wrapper.class));
        verify(passwords, never()).encode(any());
    }

    private void setupCredentials(String role) {
        when(users.selectOne(any())).thenReturn(user(role));
        when(passwords.matches("plain-password", "old-hash")).thenReturn(true);
    }

    private void setupSuccessfulCredentials(String role) {
        setupCredentials(role);
        when(sites.requireSite(2L)).thenReturn(new SiteResolver.ResolvedSite(2L, "ENABLED"));
        when(users.update(isNull(), any(Wrapper.class))).thenReturn(1);
    }

    private AdminUser user(String role) {
        AdminUser user = new AdminUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setPasswordHash("old-hash");
        user.setNickname("Administrator");
        user.setRoleCode(role);
        user.setStatus("ENABLED");
        return user;
    }

    private AdminLoginRequest login(String password) {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setUsername("admin");
        request.setPassword(password);
        return request;
    }
}
