package com.eems.security;

import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import com.eems.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    private final JwtTokenUtil tokens = mock(JwtTokenUtil.class);
    private final AdminUserMapper users = mock(AdminUserMapper.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(tokens, users,
            new SecurityFailureHandler(new ObjectMapper(), mock(OperationLogService.class)));
    private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/me");
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
        request.addHeader("Authorization", "Bearer test-token");
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authoritiesComeFromCurrentDatabaseRoleInsteadOfToken() throws Exception {
        when(tokens.parseClaims("test-token")).thenReturn(claims(Map.of("userId", 1, "loginSiteId", 2,
                "authVersion", 3, "roleCode", "SUPER_ADMIN")));
        AdminUser user = enabledUser();
        user.setRoleCode("EDITOR");
        when(users.selectById(1L)).thenReturn(user);
        filter.doFilter(request, response, chain);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("admin", authentication.getName());
        assertEquals("ROLE_EDITOR", authentication.getAuthorities().iterator().next().getAuthority());
        assertEquals(2L, ((AdminPrincipal) authentication.getPrincipal()).loginSiteId());
        verify(chain).doFilter(request, response);
    }

    @Test
    void invalidSignedTokenStopsChainWith401() throws Exception {
        when(tokens.parseClaims("test-token")).thenThrow(new MalformedJwtException("invalid"));
        filter.doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(chain, users);
    }

    @Test
    void missingVersionAndSiteClaimsRejectLegacyToken() throws Exception {
        when(tokens.parseClaims("test-token")).thenReturn(claims(Map.of("userId", 1, "roleCode", "SUPER_ADMIN")));
        filter.doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        verifyNoInteractions(chain, users);
    }

    @Test
    void fractionalIdentityClaimIsRejected() throws Exception {
        when(tokens.parseClaims("test-token")).thenReturn(claims(Map.of("userId", 1, "loginSiteId", 2.5,
                "authVersion", 3)));
        filter.doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        verifyNoInteractions(chain, users);
    }

    @Test
    void databaseFailureCannotAuthenticateOrContinue() {
        when(tokens.parseClaims("test-token")).thenReturn(claims(Map.of("userId", 1, "loginSiteId", 2, "authVersion", 3)));
        when(users.selectById(1L)).thenThrow(new IllegalStateException("database unavailable"));
        assertThrows(IllegalStateException.class, () -> filter.doFilter(request, response, chain));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(chain);
    }

    private Claims claims(Map<String, Object> values) {
        return Jwts.claims().subject("admin").add(values).build();
    }

    private AdminUser enabledUser() {
        AdminUser user = new AdminUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setStatus("ENABLED");
        user.setAuthVersion(3L);
        return user;
    }
}
