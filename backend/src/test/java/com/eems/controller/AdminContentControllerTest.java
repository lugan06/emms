package com.eems.controller;

import com.eems.config.SecurityConfig;
import com.eems.dto.ContentSaveRequest;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.ContentService;
import com.eems.vo.ContentVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminContentController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminContentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContentService contentService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void contentEndpointsShouldRequireJwt() throws Exception {
        mockMvc.perform(get("/api/admin/contents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin")
    void listShouldCallServiceForAuthenticatedUser() throws Exception {
        when(contentService.page(any())).thenReturn(null);

        mockMvc.perform(get("/api/admin/contents")
                        .param("status", "PUBLISHED")
                        .param("isTop", "1"))
                .andExpect(status().isOk());

        verify(contentService).page(any());
    }

    @Test
    @WithMockUser(username = "admin")
    void createShouldValidateAndPassUsername() throws Exception {
        when(contentService.create(any(ContentSaveRequest.class), org.mockito.ArgumentMatchers.eq("admin")))
                .thenReturn(new ContentVO(1L, 2L, null, "展会新闻", null, null, null, null, null, null,
                        "DRAFT", null, 0, 0, 0, 0, null, null, null));

        mockMvc.perform(post("/api/admin/contents")
                        .contentType("application/json")
                        .content("""
                                {
                                  "categoryId": 2,
                                  "title": "展会新闻",
                                  "sortOrder": 0,
                                  "isTop": 0,
                                  "isRecommend": 0
                                }
                                """))
                .andExpect(status().isOk());

        verify(contentService).create(any(ContentSaveRequest.class), org.mockito.ArgumentMatchers.eq("admin"));
    }
}
