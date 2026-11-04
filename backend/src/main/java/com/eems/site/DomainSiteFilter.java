package com.eems.site;

import com.eems.security.SecurityFailureHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** Servlet filter outside Spring Security: resolve Host first and own context cleanup. */
public class DomainSiteFilter extends OncePerRequestFilter {
    public static final String HOST_SITE = DomainSiteFilter.class.getName() + ".hostSite";
    private final SiteResolver resolver;
    private final SecurityFailureHandler failures;

    public DomainSiteFilter(SiteResolver resolver, SecurityFailureHandler failures) {
        this.resolver = resolver;
        this.failures = failures;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        SiteContext.clear();
        try {
            String path = SiteRequestPolicy.path(request);
            if ((!SiteRequestPolicy.isAdmin(path) || SiteRequestPolicy.isLogin(path))
                    && request.getHeader(SiteRequestPolicy.HEADER) != null) {
                throw new SiteResolutionException("SITE_OVERRIDE_NOT_ALLOWED", "此接口不接受站点切换请求头", 400);
            }
            String host = request.getHeader("Host");
            SiteResolver.ResolvedSite site = resolver.resolveHost(host == null ? request.getServerName() : host);
            request.setAttribute(HOST_SITE, site);
            SiteContext.setSiteId(site.id());
            // Defer disabled-site decisions on authenticated admin requests until identity is known.
            if (!SiteRequestPolicy.isAdmin(path) || SiteRequestPolicy.isLogin(path)) {
                SiteResolver.requireEnabled(site);
            }
            chain.doFilter(request, response);
        } catch (SiteResolutionException exception) {
            failures.reject(request, response, exception.getStatus(), exception.getCode(), exception.getMessage(), null);
        } finally {
            SiteContext.clear();
            request.removeAttribute(HOST_SITE);
        }
    }
}
