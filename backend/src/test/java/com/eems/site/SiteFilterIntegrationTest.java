package com.eems.site;

import com.eems.service.CompanyProfileService;
import com.eems.service.SiteConfigService;
import com.eems.vo.PublicSiteVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SiteFilterIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @MockBean private SiteConfigService siteConfigService;
    @MockBean private CompanyProfileService companyProfileService;

    @BeforeEach
    void addSecondSite() {
        jdbc.update("INSERT INTO site (id, name, code, status) VALUES (2, 'Second site', 'second', 'ENABLED')");
        jdbc.update("INSERT INTO site_domain (id, site_id, domain, is_primary) VALUES (3, 2, 'b.example', 1)");
        when(siteConfigService.getPublicDefault()).thenAnswer(invocation -> new PublicSiteVO(
                "site-" + SiteContext.requireSiteId(), null, null, null, null, null, null, null));
    }

    @Test
    void publicRequestsResolveDifferentSites() throws Exception {
        mockMvc.perform(get("/api/public/site").header("Host", "localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteTitle").value("site-1"));
        assertThrows(IllegalStateException.class, SiteContext::requireSiteId);
        mockMvc.perform(get("/api/public/site").header("Host", "b.example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteTitle").value("site-2"));
        assertThrows(IllegalStateException.class, SiteContext::requireSiteId);
    }

    @Test
    void localUnknownHostFallsBackButDisabledSiteDoesNot() throws Exception {
        mockMvc.perform(get("/api/public/site").header("Host", "temporary-tunnel.example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteTitle").value("site-1"));
        jdbc.update("UPDATE site SET status = 'DISABLED' WHERE id = 2");
        mockMvc.perform(get("/api/public/site").header("Host", "b.example"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SITE_DISABLED"));
        assertThrows(IllegalStateException.class, SiteContext::requireSiteId);
    }

    @Test
    void adminOnOtherSiteIsRejectedBeforeAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/site").header("Host", "b.example"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("SITE_ADMIN_FORBIDDEN"));
        assertThrows(IllegalStateException.class, SiteContext::requireSiteId);
    }

    @Test
    void malformedHostIsRejectedAndOpenApiRemainsAccessible() throws Exception {
        mockMvc.perform(get("/api/public/site").header("Host", "https://b.example"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_HOST"));
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/public/site'].get").exists());
    }

    @Test
    void clearsContextWhenControllerFails() throws Exception {
        doThrow(new IllegalStateException("simulated failure")).when(siteConfigService).getPublicDefault();
        mockMvc.perform(get("/api/public/site").header("Host", "b.example"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"));
        assertThrows(IllegalStateException.class, SiteContext::requireSiteId);
    }
}
