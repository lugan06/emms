package com.eems.controller;

import com.eems.mapper.AdminUserMapper;
import com.eems.service.OperationLogService;
import com.eems.site.SiteContextTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@WithMockUser(username = "admin")
@Sql("/public-home-fixture.sql")
class ContentCategoryIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private AdminUserMapper adminUserMapper;
    @MockitoBean private OperationLogService operationLogService;

    @BeforeEach
    void selectSite() { SiteContextTestSupport.selectDefaultSite(); }
    @AfterEach
    void clearSite() { SiteContextTestSupport.clear(); }

    @Test
    void createAndEditShouldRejectParentsIncludingDisabledAndHiddenChildren() throws Exception {
        jdbc.update("INSERT INTO cms_category (id,parent_id,name,code,is_enabled,show_in_nav) VALUES (20,2,'禁用子栏目','disabled-child',0,0)");
        String payload = "{\"categoryId\":2,\"title\":\"不能保存到父栏目\"}";
        mockMvc.perform(post("/api/admin/contents").principal(new TestingAuthenticationToken("admin", ""))
                        .contentType("application/json").content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONTENT_CATEGORY_HAS_CHILDREN"));
        mockMvc.perform(put("/api/admin/contents/102").principal(new TestingAuthenticationToken("admin", ""))
                        .contentType("application/json").content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONTENT_CATEGORY_HAS_CHILDREN"));
        assertEquals("当前展会简介", jdbc.queryForObject("SELECT title FROM cms_content WHERE id=102", String.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cms_content WHERE title='不能保存到父栏目'", Integer.class));
    }

    @Test
    void editShouldAllowLeafWithDeletedOrOtherSiteChildren() throws Exception {
        jdbc.update("INSERT INTO cms_category (id,site_id,parent_id,name,code,deleted) VALUES "
                + "(20,1,2,'已删除子栏目','deleted-child',1),(21,2,2,'其他站点子栏目','foreign-child',0)");
        mockMvc.perform(put("/api/admin/contents/102").principal(new TestingAuthenticationToken("admin", ""))
                        .contentType("application/json")
                        .content("{\"categoryId\":2,\"title\":\"末级栏目正常保存\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categoryId").value(2))
                .andExpect(jsonPath("$.data.title").value("末级栏目正常保存"));
    }

    @Test
    void openApiShouldDescribeLeafCategoryRequirement() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.ContentSaveRequest.properties.categoryId.description")
                        .value("所属栏目 ID，必须选择当前站点没有未删除子栏目的末级栏目"));
    }
}
