package com.eems.site;

import com.eems.mapper.AdminUserSiteMapper;
import com.eems.security.AdminPrincipal;
import com.eems.security.SecurityFailureHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/** Security-chain filter: runs after JWT authentication, before authorization/controllers. */
public class SiteContextFilter extends OncePerRequestFilter {
    private final SiteResolver resolver;
    private final AdminUserSiteMapper bindings;
    private final SecurityFailureHandler failures;

    public SiteContextFilter(SiteResolver resolver, AdminUserSiteMapper bindings, SecurityFailureHandler failures) {
        this.resolver = resolver;
        this.bindings = bindings;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AdminPrincipal principal)) {
            failures.reject(request, response, 401, "UNAUTHORIZED", "请先登录", null);
            return;
        }
        try {
            if (!(request.getAttribute(DomainSiteFilter.HOST_SITE) instanceof SiteResolver.ResolvedSite hostSite)) {
                throw new SiteResolutionException("SITE_CONTEXT_MISSING", "站点上下文未建立", 503);
            }
            List<String> headers = Collections.list(request.getHeaders(SiteRequestPolicy.HEADER));
            if (!headers.isEmpty() && !principal.isSuperAdmin()) {
                throw new SiteResolutionException("SITE_OVERRIDE_FORBIDDEN", "仅超级管理员可切换站点", 403);
            }
            if (!headers.isEmpty()) {
                if (!SiteRequestPolicy.allowsOverride(SiteRequestPolicy.path(request))) {
                    throw new SiteResolutionException("SITE_OVERRIDE_NOT_ALLOWED", "此接口不接受站点切换请求头", 400);
                }
                SiteContext.setSiteId(resolver.requireSite(parseSiteId(headers)).id());
            } else if (!principal.isSuperAdmin()) {
                SiteResolver.requireEnabled(hostSite);
                if (!hostSite.id().equals(principal.loginSiteId())) {
                    throw new SiteResolutionException("TOKEN_SITE_MISMATCH", "请在当前站点重新登录", 403);
                }
                if (!bindings.existsBinding(principal.userId(), hostSite.id())) {
                    throw new SiteResolutionException("SITE_ACCESS_DENIED", "没有当前站点的管理权限", 403);
                }
            }
            chain.doFilter(request, response);
        } catch (SiteResolutionException exception) {
            failures.reject(request, response, exception.getStatus(), exception.getCode(), exception.getMessage(),
                    principal.username());
        }
    }

    private Long parseSiteId(List<String> headers) {
        if (headers.size() != 1 || !headers.get(0).matches("[1-9][0-9]{0,18}")) {
            throw invalidHeader();
        }
        try {
            return Long.valueOf(headers.get(0));
        } catch (NumberFormatException exception) {
            throw invalidHeader();
        }
    }

    private SiteResolutionException invalidHeader() {
        return new SiteResolutionException("INVALID_SITE_ID", "站点请求头必须为单个有效的正整数", 400);
    }
}
