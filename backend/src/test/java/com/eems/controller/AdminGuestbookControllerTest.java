package com.eems.controller;

import com.eems.common.api.PageResult;
import com.eems.config.SecurityConfig;
import com.eems.dto.GuestbookReplyRequest;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.GuestbookService;
import com.eems.vo.GuestbookVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminGuestbookController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AdminGuestbookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GuestbookService guestbookService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void adminGuestbookEndpointsShouldRequireJwt() throws Exception {
        mockMvc.perform(get("/api/admin/guestbooks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin")
    void listShouldCallServiceForAuthenticatedAdmin() throws Exception {
        when(guestbookService.page(any())).thenReturn(new PageResult<>(java.util.List.of(), 0, 1, 20));

        mockMvc.perform(get("/api/admin/guestbooks").param("status", "UNREAD"))
                .andExpect(status().isOk());

        verify(guestbookService).page(any());
    }

    @Test
    @WithMockUser(username = "admin")
    void replyShouldPassAuthenticatedUsername() throws Exception {
        when(guestbookService.reply(eq(1L), any(GuestbookReplyRequest.class), eq("admin")))
                .thenReturn(null);

        mockMvc.perform(post("/api/admin/guestbooks/1/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"replyContent\":\"您好，展位申请已开放。\"}"))
                .andExpect(status().isOk());

        verify(guestbookService).reply(eq(1L), any(GuestbookReplyRequest.class), eq("admin"));
    }

    @Test
    @WithMockUser(username = "admin")
    void readAndCloseShouldCallService() throws Exception {
        when(guestbookService.markRead(2L)).thenReturn((GuestbookVO) null);
        when(guestbookService.close(3L)).thenReturn((GuestbookVO) null);

        mockMvc.perform(post("/api/admin/guestbooks/2/read"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/admin/guestbooks/3/close"))
                .andExpect(status().isOk());

        verify(guestbookService).markRead(2L);
        verify(guestbookService).close(3L);
    }
}
