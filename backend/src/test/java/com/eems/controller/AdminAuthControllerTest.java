package com.eems.controller;

import com.eems.service.AdminAuthService;
import com.eems.security.JwtTokenUtil;
import com.eems.vo.AdminLoginResponse;
import com.eems.vo.AdminUserVO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAuthService adminAuthService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void loginShouldReturnJwtInResponseHeaderAndBody() throws Exception {
        AdminLoginResponse response = new AdminLoginResponse(
                "jwt-token",
                "Bearer",
                7200,
                new AdminUserVO(1L, "admin", "Administrator", null, "SUPER_ADMIN"));
        when(adminAuthService.login(any(), eq("10.0.0.8"))).thenReturn(response);

        mockMvc.perform(post("/api/admin/login")
                        .header("X-Forwarded-For", "10.0.0.8, 10.0.0.1")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"plain-password\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", "Bearer jwt-token"))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.username").value("admin"));

        ArgumentCaptor<com.eems.dto.AdminLoginRequest> requestCaptor =
                ArgumentCaptor.forClass(com.eems.dto.AdminLoginRequest.class);
        verify(adminAuthService).login(requestCaptor.capture(), eq("10.0.0.8"));
        assertEquals("admin", requestCaptor.getValue().getUsername());
        assertEquals("plain-password", requestCaptor.getValue().getPassword());
    }

    @Test
    void loginShouldRejectBlankCredentials() throws Exception {
        mockMvc.perform(post("/api/admin/login")
                        .contentType(APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void meShouldReturnCurrentAdminProfile() throws Exception {
        when(adminAuthService.currentUser("admin"))
                .thenReturn(new AdminUserVO(1L, "admin", "Administrator", null, "SUPER_ADMIN"));

        mockMvc.perform(get("/api/admin/me")
                        .principal(new UsernamePasswordAuthenticationToken("admin", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.roleCode").value("SUPER_ADMIN"));
    }
}
