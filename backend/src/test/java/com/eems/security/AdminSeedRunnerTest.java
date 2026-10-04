package com.eems.security;

import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminSeedRunnerTest {

    @Test
    void enabledSeedShouldCreateHashedAdmin() {
        AdminSeedProperties properties = properties(true);
        AdminUserMapper mapper = mock(AdminUserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(mapper.selectOne(any())).thenReturn(null);
        when(encoder.encode("plain-password")).thenReturn("bcrypt-hash");

        new AdminSeedRunner(properties, mapper, encoder).run();

        ArgumentCaptor<AdminUser> adminCaptor = ArgumentCaptor.forClass(AdminUser.class);
        verify(mapper).insert(adminCaptor.capture());
        AdminUser created = adminCaptor.getValue();
        assertEquals("admin", created.getUsername());
        assertEquals("bcrypt-hash", created.getPasswordHash());
        assertEquals("ENABLED", created.getStatus());
        assertEquals(0, created.getDeleted());
        verify(encoder).encode("plain-password");
    }

    @Test
    void enabledSeedShouldSkipExistingUsername() {
        AdminSeedProperties properties = properties(true);
        AdminUserMapper mapper = mock(AdminUserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(mapper.selectOne(any())).thenReturn(new AdminUser());

        new AdminSeedRunner(properties, mapper, encoder).run();

        verify(mapper, never()).insert(any(AdminUser.class));
        verify(encoder, never()).encode(any());
    }

    @Test
    void disabledSeedShouldNotQueryDatabase() {
        AdminSeedProperties properties = properties(false);
        AdminUserMapper mapper = mock(AdminUserMapper.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);

        new AdminSeedRunner(properties, mapper, encoder).run();

        verify(mapper, never()).selectOne(any());
        verify(mapper, never()).insert(any(AdminUser.class));
        verify(encoder, never()).encode(any());
    }

    private AdminSeedProperties properties(boolean enabled) {
        AdminSeedProperties properties = new AdminSeedProperties();
        properties.setEnabled(enabled);
        properties.setUsername("admin");
        properties.setPassword("plain-password");
        properties.setNickname("Administrator");
        properties.setRoleCode("SUPER_ADMIN");
        return properties;
    }
}
