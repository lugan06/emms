package com.eems.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenUtil);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validBearerTokenShouldPopulateSecurityContext() throws Exception {
        Claims claims = mock(Claims.class);
        when(jwtTokenUtil.isValid("jwt-token")).thenReturn(true);
        when(jwtTokenUtil.parseClaims("jwt-token")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("admin");
        when(claims.get("roleCode", String.class)).thenReturn("SUPER_ADMIN");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer jwt-token");
        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("admin", authentication.getName());
        assertEquals("ROLE_SUPER_ADMIN", authentication.getAuthorities().iterator().next().getAuthority());
        verify(filterChain).doFilter(eq(request), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void invalidBearerTokenShouldNotAuthenticateRequest() throws Exception {
        when(jwtTokenUtil.isValid("invalid-token")).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        filter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(eq(request), org.mockito.ArgumentMatchers.any());
    }
}
