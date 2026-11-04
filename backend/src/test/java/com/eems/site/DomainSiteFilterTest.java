package com.eems.site;

import com.eems.security.SecurityFailureHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DomainSiteFilterTest {
    private final SiteResolver resolver = mock(SiteResolver.class);
    private final SecurityFailureHandler failures = mock(SecurityFailureHandler.class);
    private final DomainSiteFilter filter = new DomainSiteFilter(resolver, failures);

    @AfterEach
    void clear() {
        SiteContext.clear();
    }

    @Test
    void preflightDoesNotResolveHostOrEvaluateOverride() throws Exception {
        var request = new MockHttpServletRequest("OPTIONS", "/api/admin/site");
        request.addHeader("Host", "malformed:bad");
        request.addHeader("X-Site-Id", "invalid");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verifyNoInteractions(resolver, failures);
        assertNull(SiteContext.currentSiteIdOrNull());
    }

    @Test
    void unexpectedDownstreamFailureStillClearsHostAndEffectiveContext() {
        var request = new MockHttpServletRequest("GET", "/api/admin/site");
        request.addHeader("Host", "a.example");
        when(resolver.resolveHost("a.example")).thenReturn(new SiteResolver.ResolvedSite(1L, "ENABLED"));
        assertThrows(ServletException.class, () -> filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            assertEquals(1L, SiteContext.requireSiteId());
            SiteContext.setSiteId(2L);
            throw new ServletException("simulated downstream failure");
        }));
        assertNull(SiteContext.currentSiteIdOrNull());
        assertNull(request.getAttribute(DomainSiteFilter.HOST_SITE));
    }
}
