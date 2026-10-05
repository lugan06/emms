package com.eems.controller;

import com.eems.config.SecurityConfig;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.CompanyProfileService;
import com.eems.service.SiteConfigService;
import com.eems.vo.PublicCompanyVO;
import com.eems.vo.PublicSiteVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicSiteController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PublicSiteControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SiteConfigService siteConfigService;

    @MockBean
    private CompanyProfileService companyProfileService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void publicSiteShouldBeReadableWithoutTokenAndHideInternalFields() throws Exception {
        when(siteConfigService.getPublicDefault()).thenReturn(new PublicSiteVO(
                "EEMS", "Public subtitle", "example.test", "/uploads/logo.png",
                "coating", "Public description", "沪ICP备00000000号", "Footer"));

        mockMvc.perform(get("/api/public/site"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteTitle").value("EEMS"))
                .andExpect(jsonPath("$.data.statisticsCode").doesNotExist())
                .andExpect(jsonPath("$.data.templateCode").doesNotExist())
                .andExpect(jsonPath("$.data.id").doesNotExist());
    }

    @Test
    void publicCompanyShouldOnlyExposePublicContactFields() throws Exception {
        when(companyProfileService.getPublicDefault()).thenReturn(new PublicCompanyVO(
                "EEMS Company", "Shanghai", "200000", "Wang", "021-60000000",
                "service@example.test", "/uploads/wechat.png"));

        mockMvc.perform(get("/api/public/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("EEMS Company"))
                .andExpect(jsonPath("$.data.telephone").value("021-60000000"))
                .andExpect(jsonPath("$.data.businessLicenseNo").doesNotExist())
                .andExpect(jsonPath("$.data.mobile").doesNotExist())
                .andExpect(jsonPath("$.data.profileCode").doesNotExist());
    }
}
