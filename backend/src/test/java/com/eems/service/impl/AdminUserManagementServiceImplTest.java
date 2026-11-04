package com.eems.service.impl;

import com.eems.common.exception.BusinessException;
import com.eems.dto.AdminUserCreateRequest;
import com.eems.dto.AdminUserUpdateRequest;
import com.eems.entity.AdminUser;
import com.eems.mapper.*;
import com.eems.service.OperationLogService;
import com.eems.site.SiteResolutionException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminUserManagementServiceImplTest {
    private final AdminUserMapper users = mock(AdminUserMapper.class);
    private final AdminUserSiteMapper bindings = mock(AdminUserSiteMapper.class);
    private final SiteManagementMapper sites = mock(SiteManagementMapper.class);
    private final PasswordEncoder passwords = mock(PasswordEncoder.class);
    private final OperationLogService logs = mock(OperationLogService.class);
    private final AdminUserManagementServiceImpl service = new AdminUserManagementServiceImpl(users, bindings, sites, passwords, logs);

    private AdminUser editor() {
        AdminUser u = new AdminUser(); u.setId(7L); u.setRoleCode("EDITOR"); u.setDeleted(0);
        u.setStatus("ENABLED"); u.setAuthVersion(3L); return u;
    }

    @Test
    void defendsSuperAccountsEvenIfMapperReturnsWrongRole() {
        AdminUser u = editor(); u.setRoleCode("SUPER_ADMIN");
        when(users.lockEditor(7L)).thenReturn(u);
        assertThrows(SiteResolutionException.class, () -> service.update(7L, new AdminUserUpdateRequest("Nope"), "super"));
        assertThrows(SiteResolutionException.class, () -> service.assignSites(7L, List.of(), "super"));
        assertThrows(SiteResolutionException.class, () -> service.changeStatus(7L, "DISABLED", "super"));
        assertThrows(SiteResolutionException.class, () -> service.resetPassword(7L, "valid-password", "super"));
        verifyNoInteractions(bindings, sites, passwords, logs);
        verify(users, never()).update(isNull(), any());
    }

    @Test
    void rejectsMissingOrDuplicateSitesBeforeDeletingBindings() {
        when(users.lockEditor(7L)).thenReturn(editor());
        when(sites.selectCount(any())).thenReturn(0L);
        assertThrows(BusinessException.class, () -> service.assignSites(7L, List.of(1L), "super"));
        assertThrows(BusinessException.class, () -> service.assignSites(7L, List.of(1L, 1L), "super"));
        verifyNoInteractions(bindings, logs);
    }

    @Test
    void failedConditionalVersionUpdateCannotBeReportedAsSuccess() {
        when(users.lockEditor(7L)).thenReturn(editor());
        when(passwords.encode("valid-password")).thenReturn("encoded");
        when(users.update(isNull(), any())).thenReturn(0);
        assertEquals("ADMIN_STATE_CHANGED", assertThrows(BusinessException.class,
                () -> service.resetPassword(7L, "valid-password", "super")).getCode());
        verifyNoInteractions(logs);
    }

    @Test
    void overflowCannotWrapAuthenticationVersion() {
        AdminUser user = editor(); user.setAuthVersion(Long.MAX_VALUE);
        when(users.lockEditor(7L)).thenReturn(user);
        assertEquals("AUTH_VERSION_EXHAUSTED", assertThrows(BusinessException.class,
                () -> service.changeStatus(7L, "DISABLED", "super")).getCode());
        verify(users, never()).update(isNull(), any());
        verifyNoInteractions(logs);
    }

    @Test
    void rejectsBcryptByteOverflowWithoutEncoding() {
        assertThrows(BusinessException.class, () -> service.create(
                new AdminUserCreateRequest("u", "密".repeat(25), "Nickname", List.of()), "super"));
        verifyNoInteractions(users, bindings, sites, passwords, logs);
    }

    @Test
    void duplicateUsernameIsAReadableBusinessFailure() {
        when(passwords.encode(anyString())).thenReturn("encoded");
        when(users.insert(any(AdminUser.class))).thenThrow(new DuplicateKeyException("username unique"));
        assertEquals("ADMIN_USERNAME_EXISTS", assertThrows(BusinessException.class,
                () -> service.create(new AdminUserCreateRequest("u", "valid-password", "Name", List.of()), "super")).getCode());
        verifyNoInteractions(bindings, logs);
    }
}

