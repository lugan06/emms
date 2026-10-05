package com.eems.controller;

import com.eems.config.SecurityConfig;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.ExhibitionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(AdminExhibitionController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminExhibitionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExhibitionService exhibitionService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void exhibitionEndpointsShouldRequireJwt() throws Exception {
        mockMvc.perform(get("/api/admin/exhibitions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin")
    void restoreShouldCallServiceForAuthenticatedUser() throws Exception {
        when(exhibitionService.restore(6L, "admin"))
                .thenReturn(mock(com.eems.vo.ExhibitionVO.class));

        mockMvc.perform(post("/api/admin/exhibitions/6/restore"))
                .andExpect(status().isOk());

        verify(exhibitionService).restore(6L, "admin");
    }

    @Test
    @WithMockUser(username = "admin")
    void republishShouldCallServiceForAuthenticatedUser() throws Exception {
        when(exhibitionService.republish(8L, "admin"))
                .thenReturn(mock(com.eems.vo.ExhibitionVO.class));

        mockMvc.perform(post("/api/admin/exhibitions/8/republish"))
                .andExpect(status().isOk());

        verify(exhibitionService).republish(8L, "admin");
    }

}
