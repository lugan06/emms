package com.eems.controller;

import com.eems.security.JwtTokenUtil;
import com.eems.service.CompanyProfileService;
import com.eems.service.SiteConfigService;
import com.eems.vo.CompanyProfileVO;
import com.eems.vo.SiteConfigVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({AdminSiteController.class, AdminCompanyController.class})
@AutoConfigureMockMvc(addFilters = false)
class AdminConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SiteConfigService siteConfigService;

    @MockBean
    private CompanyProfileService companyProfileService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void siteEndpointsShouldReadAndUpdateDefaultConfig() throws Exception {
        when(siteConfigService.getDefault()).thenReturn(new SiteConfigVO(
                1L, "default", null, "EEMS", null, null, null, null,
                null, null, null, null, null));
        when(siteConfigService.updateDefault(any())).thenReturn(new SiteConfigVO(
                1L, "default", null, "Updated EEMS", null, null, null, null,
                null, null, null, null, null));

        mockMvc.perform(get("/api/admin/site"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteTitle").value("EEMS"));
        mockMvc.perform(put("/api/admin/site")
                        .contentType(APPLICATION_JSON)
                        .content("{\"siteTitle\":\"Updated EEMS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteTitle").value("Updated EEMS"));
    }

    @Test
    void siteUpdateShouldReturnClientErrorForMissingExhibition() throws Exception {
        when(siteConfigService.updateDefault(any()))
                .thenThrow(new com.eems.common.exception.BusinessException(
                        "EXHIBITION_NOT_FOUND", "当前主展会不存在或已删除"));

        mockMvc.perform(put("/api/admin/site")
                        .contentType(APPLICATION_JSON)
                        .content("{\"siteTitle\":\"Updated EEMS\",\"currentExhibitionId\":44}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EXHIBITION_NOT_FOUND"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void companyEndpointsShouldReadAndValidateUpdate() throws Exception {
        when(companyProfileService.getDefault()).thenReturn(new CompanyProfileVO(
                1L, "default", "EEMS Company", null, null, null, null, null,
                null, "test@example.com", null, null, null, null));

        mockMvc.perform(get("/api/admin/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("EEMS Company"));
        mockMvc.perform(put("/api/admin/company")
                        .contentType(APPLICATION_JSON)
                        .content("{\"companyName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
