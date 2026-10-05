package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.config.SecurityConfig;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.FileAssetService;
import com.eems.vo.FileAssetVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(FileAssetController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class FileAssetControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileAssetService fileAssetService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void fileEndpointsShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/files"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authenticatedUserShouldUploadListAndDeleteFile() throws Exception {
        FileAssetVO file = new FileAssetVO(1L, "logo.png", "/uploads/random.png", "image/png",
                12L, 1L, LocalDateTime.of(2026, 10, 5, 12, 0));
        when(fileAssetService.upload(any(), eq("admin"))).thenReturn(file);
        when(fileAssetService.page(any())).thenReturn(new PageResult<>(List.of(file), 1, 1, 20));
        doNothing().when(fileAssetService).delete(1L);

        mockMvc.perform(multipart("/api/admin/files")
                        .file(new MockMultipartFile("file", "logo.png", "image/png", new byte[]{1, 2, 3}))
                        .with(SecurityMockMvcRequestPostProcessors.user("admin"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileUrl").value("/uploads/random.png"));
        mockMvc.perform(get("/api/admin/files")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].originalName").value("logo.png"));
        mockMvc.perform(delete("/api/admin/files/1")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
