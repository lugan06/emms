package com.eems.site;

import com.eems.common.api.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SiteContextFilter extends OncePerRequestFilter {
    private final SiteResolver siteResolver;
    private final ObjectMapper objectMapper;

    public SiteContextFilter(SiteResolver siteResolver, ObjectMapper objectMapper) {
        this.siteResolver = siteResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String host = request.getHeader("Host");
            if (host == null) {
                host = request.getServerName();
            }
            boolean adminRequest = request.getRequestURI().substring(request.getContextPath().length())
                    .startsWith("/api/admin/");
            SiteContext.setSiteId(siteResolver.resolve(host, adminRequest));
            filterChain.doFilter(request, response);
        } catch (SiteResolutionException exception) {
            response.setStatus(exception.getStatus());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(), Result.failure(exception.getCode(), exception.getMessage(), null));
        } finally {
            SiteContext.clear();
        }
    }
}
