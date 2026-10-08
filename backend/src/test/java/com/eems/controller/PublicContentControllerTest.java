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

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicContentController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PublicContentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PublicContentService publicContentService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void publicContentDetailShouldNotRequireJwt() throws Exception {
        mockMvc.perform(get("/api/public/contents/10"))
                .andExpect(status().isOk());

        verify(publicContentService).get(10L);
    }
}
