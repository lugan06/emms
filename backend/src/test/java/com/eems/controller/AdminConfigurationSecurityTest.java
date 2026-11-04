package com.eems.controller;

import com.eems.security.JwtTokenUtil;
import com.eems.service.CompanyProfileService;
import com.eems.service.SiteConfigService;
import com.eems.vo.SiteConfigVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminConfigurationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteConfigService siteConfigService;

    @MockitoBean
    private CompanyProfileService companyProfileService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired private JdbcTemplate jdbc;

    @BeforeEach
    void createAuthenticatedAccounts() {
        jdbc.update("INSERT INTO admin_user (id, username, password_hash, nickname, role_code) "
                + "VALUES (65001, 'config-admin', 'test-placeholder', 'Admin', 'SUPER_ADMIN'), "
                + "(65002, 'config-editor', 'test-placeholder', 'Editor', 'EDITOR')");
        jdbc.update("INSERT INTO admin_user_site (admin_user_id, site_id) VALUES (65002, 1)");
    }

    @Test
    void editorShouldNotUpdateSiteConfig() throws Exception {
        mockMvc.perform(put("/api/admin/site")
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateToken(65002L, "config-editor", "EDITOR", 1L, 0L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"siteTitle\":\"Updated\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminShouldUpdateSiteConfig() throws Exception {
        when(siteConfigService.updateDefault(any(), any())).thenReturn(new SiteConfigVO(
                1L, "default", null, "Updated", null, null, null, null,
                null, null, null, null, null, null));

        mockMvc.perform(put("/api/admin/site")
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateToken(65001L, "config-admin", "SUPER_ADMIN", 1L, 0L))
                        .contentType(APPLICATION_JSON)
                        .content("{\"siteTitle\":\"Updated\"}"))
                .andExpect(status().isOk());
    }
}
