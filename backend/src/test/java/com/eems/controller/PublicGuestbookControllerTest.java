package com.eems.controller;

import com.eems.config.SecurityConfig;
import com.eems.dto.GuestbookCreateRequest;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.GuestbookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicGuestbookController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PublicGuestbookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GuestbookService guestbookService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void publicGuestbookShouldNotRequireJwt() throws Exception {
        mockMvc.perform(post("/api/public/guestbook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "张先生",
                                  "phone": "13800138000",
                                  "email": "contact@example.com",
                                  "message": "请问如何申请展位？"
                                }
                                """))
                .andExpect(status().isOk());

        verify(guestbookService).create(any(GuestbookCreateRequest.class), eq("127.0.0.1"));
    }

    @Test
    void publicGuestbookShouldValidateRequiredFields() throws Exception {
        mockMvc.perform(post("/api/public/guestbook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"message\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
