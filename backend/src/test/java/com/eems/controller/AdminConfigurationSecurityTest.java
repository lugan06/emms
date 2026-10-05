package com.eems.controller;

import com.eems.security.JwtTokenUtil;
import com.eems.service.CompanyProfileService;
import com.eems.service.SiteConfigService;
import com.eems.vo.SiteConfigVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminConfigurationSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SiteConfigService siteConfigService;

    @MockBean
    private CompanyProfileService companyProfileService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void editorShouldNotUpdateSiteConfig() throws Exception {
        mockMvc.perform(put("/api/admin/site")
                        .with(user("editor").roles("EDITOR"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"siteTitle\":\"Updated\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminShouldUpdateSiteConfig() throws Exception {
        when(siteConfigService.updateDefault(any())).thenReturn(new SiteConfigVO(
                1L, "default", null, "Updated", null, null, null, null,
                null, null, null, null, null));

        mockMvc.perform(put("/api/admin/site")
                        .with(user("admin").roles("SUPER_ADMIN"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"siteTitle\":\"Updated\"}"))
                .andExpect(status().isOk());
    }
}
