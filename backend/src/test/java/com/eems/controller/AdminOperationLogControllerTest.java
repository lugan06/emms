package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.config.SecurityConfig;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.OperationLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminOperationLogController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminOperationLogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OperationLogService operationLogService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void logQueryShouldRequireJwt() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "editor", roles = "EDITOR")
    void editorShouldNotAccessLogs() throws Exception {
        mockMvc.perform(get("/api/admin/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "SUPER_ADMIN")
    void superAdminShouldQueryLogsWithFilters() throws Exception {
        when(operationLogService.page(any())).thenReturn(new PageResult<>(java.util.List.of(), 0, 1, 20));

        mockMvc.perform(get("/api/admin/logs")
                        .param("username", "admin")
                        .param("module", "CMS_CONTENT")
                        .param("operation", "发布")
                        .param("result", "SUCCESS")
                        .param("startAt", "2026-10-01 00:00:00")
                        .param("endAt", "2026-10-31 23:59:59"))
                .andExpect(status().isOk());

        verify(operationLogService).page(any());
    }

    @Test
    @WithMockUser(username = "admin", roles = "SUPER_ADMIN")
    void invalidResultShouldFailValidation() throws Exception {
        mockMvc.perform(get("/api/admin/logs").param("result", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }
}
