package com.eems.controller;

import com.eems.config.SecurityConfig;
import com.eems.security.JwtAuthenticationFilter;
import com.eems.security.JwtTokenUtil;
import com.eems.service.ExhibitionService;
import com.eems.vo.PublicExhibitionVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicExhibitionController.class)
@AutoConfigureMockMvc
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class PublicExhibitionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExhibitionService exhibitionService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void publicExhibitionListShouldBeAccessibleWithoutToken() throws Exception {
        PublicExhibitionVO value = value(1L, true);
        when(exhibitionService.publicPage(any())).thenReturn(
                new com.eems.common.api.PageResult<>(List.of(value), 1, 1, 20));

        mockMvc.perform(get("/api/public/exhibitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].title").value("公开展会"))
                .andExpect(jsonPath("$.data.records[0].isCurrent").value(true));
    }

    @Test
    void publicExhibitionDetailShouldBeAccessibleWithoutToken() throws Exception {
        when(exhibitionService.publicGet(1L)).thenReturn(value(1L, false));

        mockMvc.perform(get("/api/public/exhibitions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exhibitionCode").value("EEMS-2026"))
                .andExpect(jsonPath("$.data.extraData").doesNotExist())
                .andExpect(jsonPath("$.data.createdBy").doesNotExist());
    }

    private PublicExhibitionVO value(Long id, boolean current) {
        return new PublicExhibitionVO(id, "EEMS-2026", "公开展会", null, 2026, null,
                "/uploads/exhibition.png", "简介", "详情", "展馆", "地址",
                LocalDateTime.of(2026, 6, 10, 9, 0), LocalDateTime.of(2026, 6, 12, 17, 0),
                LocalDateTime.of(2026, 1, 1, 9, 0), "联系人", "021-60000000",
                "https://example.test/register", 1, 1, current);
    }
}
