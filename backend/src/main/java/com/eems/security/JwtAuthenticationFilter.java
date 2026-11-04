package com.eems.security;

import com.eems.entity.AdminUser;
import com.eems.mapper.AdminUserMapper;
import com.eems.site.SiteRequestPolicy;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenUtil tokens;
    private final AdminUserMapper users;
    private final SecurityFailureHandler failures;

    public JwtAuthenticationFilter(JwtTokenUtil tokens, AdminUserMapper users, SecurityFailureHandler failures) {
        this.tokens = tokens;
        this.users = users;
        this.failures = failures;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = SiteRequestPolicy.path(request);
        return "OPTIONS".equalsIgnoreCase(request.getMethod()) || !SiteRequestPolicy.isAdmin(path)
                || SiteRequestPolicy.isLogin(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            chain.doFilter(request, response);
            return;
        }
        Claims claims;
        Long userId;
        Long loginSiteId;
        Long authVersion;
        try {
            if (!authorization.startsWith("Bearer ") || authorization.length() <= 7) {
                throw new IllegalArgumentException("Missing bearer token");
            }
            claims = tokens.parseClaims(authorization.substring(7));
            userId = requiredLong(claims, "userId", 1);
            loginSiteId = requiredLong(claims, "loginSiteId", 1);
            authVersion = requiredLong(claims, "authVersion", 0);
            if (claims.getSubject() == null || claims.getSubject().isBlank()) {
                throw new IllegalArgumentException("Missing subject");
            }
        } catch (JwtException | IllegalArgumentException exception) {
            reject(request, response);
            return;
        }
        // Do not catch database errors as token failures: an unavailable database must fail closed.
        AdminUser user = users.selectById(userId);
        if (user == null || !"ENABLED".equals(user.getStatus())
                || (user.getDeleted() != null && user.getDeleted() != 0)
                || !claims.getSubject().equals(user.getUsername())
                || !authVersion.equals(user.getAuthVersion())
                || !("SUPER_ADMIN".equals(user.getRoleCode()) || "EDITOR".equals(user.getRoleCode()))) {
            reject(request, response);
            return;
        }
        AdminPrincipal principal = new AdminPrincipal(user.getId(), user.getUsername(), user.getRoleCode(), loginSiteId);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + principal.roleCode())));
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private Long requiredLong(Claims claims, String name, long minimum) {
        Object value = claims.get(name);
        if (!(value instanceof Integer || value instanceof Long)) {
            throw new IllegalArgumentException("Invalid identity claim");
        }
        long number = ((Number) value).longValue();
        if (number < minimum) throw new IllegalArgumentException("Invalid identity claim");
        return number;
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        failures.reject(request, response, 401, "UNAUTHORIZED", "登录已失效，请重新登录", null);
    }
}
