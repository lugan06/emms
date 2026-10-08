package com.eems.controller;

import com.eems.config.SecurityConfig;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.PublicContentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicCategoryController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PublicCategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicContentService publicContentService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void publicCategoryTreeShouldNotRequireJwt() throws Exception {
        when(publicContentService.categoryTree()).thenReturn(List.of());

        mockMvc.perform(get("/api/public/categories/tree"))
                .andExpect(status().isOk());

        verify(publicContentService).categoryTree();
    }

    @Test
    void publicCategoryContentsShouldCallServiceWithoutToken() throws Exception {
        when(publicContentService.pageByCategory(eq("news"), any())).thenReturn(null);

        mockMvc.perform(get("/api/public/categories/news/contents")
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk());

        verify(publicContentService).pageByCategory(eq("news"), any());
    }
}
