package com.eems.controller;

import com.eems.config.SecurityConfig;
import com.eems.dto.CategorySaveRequest;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.CategoryService;
import com.eems.vo.CategoryVO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCategoryController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminCategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void categoryEndpointsShouldRequireJwt() throws Exception {
        mockMvc.perform(get("/api/admin/categories/tree"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin")
    void treeShouldCallServiceForAuthenticatedUser() throws Exception {
        when(categoryService.tree()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/categories/tree"))
                .andExpect(status().isOk());

        verify(categoryService).tree();
    }

    @Test
    @WithMockUser(username = "admin")
    void createShouldValidateAndCallService() throws Exception {
        when(categoryService.create(any(CategorySaveRequest.class)))
                .thenReturn(new CategoryVO(1L, 0L, "新闻资讯", "news", null, "ARTICLE", null,
                        null, 0, 1, 1, null, null, null, List.of()));

        mockMvc.perform(post("/api/admin/categories")
                        .contentType("application/json")
                        .content("""
                                {
                                  "parentId": 0,
                                  "name": "新闻资讯",
                                  "code": "news",
                                  "modelCode": "ARTICLE",
                                  "sortOrder": 0,
                                  "isEnabled": 1,
                                  "showInNav": 1
                                }
                                """))
                .andExpect(status().isOk());

        verify(categoryService).create(any(CategorySaveRequest.class));
    }

    @Test
    @WithMockUser(username = "admin")
    void deleteShouldReturnCategoryData() throws Exception {
        when(categoryService.delete(4L))
                .thenReturn(new CategoryVO(4L, 0L, "新闻资讯", "news", null, "ARTICLE", null,
                        null, 0, 1, 1, null, null, null, List.of()));

        mockMvc.perform(delete("/api/admin/categories/4"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.id").value(4))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.code").value("news"));

        verify(categoryService).delete(4L);
    }

    @Test
    @WithMockUser(username = "admin")
    void restoreShouldReturnCategoryData() throws Exception {
        when(categoryService.restore(4L))
                .thenReturn(new CategoryVO(4L, 2L, "行业新闻", "industry-news", null, "ARTICLE", null,
                        null, 1, 1, 1, null, null, null, List.of()));

        mockMvc.perform(post("/api/admin/categories/4/restore"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.id").value(4))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.code").value("industry-news"));

        verify(categoryService).restore(4L);
    }
}
