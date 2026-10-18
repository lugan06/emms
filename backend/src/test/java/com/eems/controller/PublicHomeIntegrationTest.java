package com.eems.controller;

import com.eems.entity.Exhibition;
import com.eems.service.CompanyProfileService;
import com.eems.service.ExhibitionService;
import com.eems.service.SiteConfigService;
import com.eems.vo.PublicCompanyVO;
import com.eems.vo.PublicExhibitionVO;
import com.eems.vo.PublicSiteVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql("/public-home-fixture.sql")
class PublicHomeIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private JdbcTemplate jdbc;
    @MockBean private SiteConfigService siteConfigService;
    @MockBean private CompanyProfileService companyProfileService;
    @MockBean private ExhibitionService exhibitionService;

    @BeforeEach
    void configureCurrentExhibition() {
        when(siteConfigService.getCurrentExhibitionId()).thenReturn(1L);
        when(siteConfigService.getPublicDefault()).thenReturn(new PublicSiteVO("测试展会", null, null, null, null, null, null, null));
        when(companyProfileService.getPublicDefault()).thenReturn(new PublicCompanyVO("测试公司", null, null, null, null, null, null));
        Exhibition exhibition = new Exhibition();
        exhibition.setId(1L);
        exhibition.setTitle("当前展会");
        when(exhibitionService.publicGet(1L)).thenReturn(PublicExhibitionVO.from(exhibition, true));
    }

    @Test
    void homeShouldSelectPublishedCurrentExhibitionContentUsingRealSql() throws Exception {
        mockMvc.perform(get("/api/public/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentExhibition.id").value(1))
                .andExpect(jsonPath("$.data.exhibitionIntro", hasSize(2)))
                .andExpect(jsonPath("$.data.exhibitionIntro[0].id").value(602))
                .andExpect(jsonPath("$.data.exhibitionIntro[1].id").value(102))
                .andExpect(jsonPath("$.data.exhibitionIntro[1].body").value("<p>当前展会正文</p>"))
                .andExpect(jsonPath("$.data.exhibitionIntro[*].exhibitionId", everyItem(is(1))))
                .andExpect(jsonPath("$.data.exhibitionScope", hasSize(1)))
                .andExpect(jsonPath("$.data.exhibitionScope[0].id").value(104))
                .andExpect(jsonPath("$.data.exhibitionScope[0].body").value("<p>参展范围</p>"))
                .andExpect(jsonPath("$.data.samePeriodActivities", hasSize(1)))
                .andExpect(jsonPath("$.data.samePeriodActivities[0].id").value(109))
                .andExpect(jsonPath("$.data.samePeriodActivities[0].body").value("<p>同期活动</p>"))
                .andExpect(jsonPath("$.data.samePeriodActivities[0].categoryCode").value("events"))
                .andExpect(jsonPath("$.data.exhibitionNews", hasSize(1)))
                .andExpect(jsonPath("$.data.exhibitionNews[0].id").value(106))
                .andExpect(jsonPath("$.data.exhibitionNews[0].body").value("<p>展会动态</p>"))
                .andExpect(jsonPath("$.data.industryNews", hasSize(2)))
                .andExpect(jsonPath("$.data.industryNews[0].body").value("<p>行业资讯</p>"))
                .andExpect(jsonPath("$.data.exhibitorNews", hasSize(1)))
                .andExpect(jsonPath("$.data.exhibitorNews[0].body").value("<p>展商新闻</p>"));
    }

    @Test
    void categoryListAndDetailShouldRemainIndependentOfHomeExhibitionFilter() throws Exception {
        mockMvc.perform(get("/api/public/categories/exhibition-intro/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(3))
                .andExpect(jsonPath("$.data.records[*].id", hasItem(1)))
                .andExpect(jsonPath("$.data.records[0].body").doesNotExist())
                .andExpect(jsonPath("$.data.records[*].id", hasItem(102)));
        mockMvc.perform(get("/api/public/contents/102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(102))
                .andExpect(jsonPath("$.data.exhibitionId").value(1))
                .andExpect(jsonPath("$.data.body").value("<p>当前展会正文</p>"));
        for (long id : new long[] {302, 402, 502}) {
            mockMvc.perform(get("/api/public/contents/" + id))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("PUBLIC_CONTENT_NOT_FOUND"));
        }
    }

    @Test
    void missingOrDisabledCategoriesShouldNotBreakOtherHomeSections() throws Exception {
        jdbc.update("DELETE FROM cms_category WHERE id = 8");
        jdbc.update("UPDATE cms_category SET is_enabled = 0 WHERE id = 9");
        mockMvc.perform(get("/api/public/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exhibitorNews", hasSize(0)))
                .andExpect(jsonPath("$.data.samePeriodActivities", hasSize(0)))
                .andExpect(jsonPath("$.data.exhibitionIntro", hasSize(2)));
    }

    @Test
    void homeShouldKeepSectionLimitAndOrdering() throws Exception {
        for (long id = 700; id < 710; id++) {
            jdbc.update("INSERT INTO cms_content (id, category_id, exhibition_id, title, status, published_at) VALUES (?, 2, 1, ?, 'PUBLISHED', '2020-01-01 00:00:00')", id, "更多简介 " + id);
        }
        mockMvc.perform(get("/api/public/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exhibitionIntro", hasSize(6)))
                .andExpect(jsonPath("$.data.exhibitionIntro[0].id").value(602))
                .andExpect(jsonPath("$.data.exhibitionIntro[1].id").value(709));
    }

    @Test
    void noCurrentExhibitionShouldKeepScopedSectionsEmptyAndGlobalNewsAvailable() throws Exception {
        when(siteConfigService.getCurrentExhibitionId()).thenReturn(null);
        mockMvc.perform(get("/api/public/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exhibitionIntro", hasSize(0)))
                .andExpect(jsonPath("$.data.exhibitionScope", hasSize(0)))
                .andExpect(jsonPath("$.data.samePeriodActivities", hasSize(0)))
                .andExpect(jsonPath("$.data.exhibitionNews", hasSize(0)))
                .andExpect(jsonPath("$.data.industryNews", hasSize(2)));
    }

    @Test
    void openApiShouldStillDescribeHomeSectionsAndCompanyEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value("3.0.1"))
                .andExpect(jsonPath("$.paths['/api/public/home'].get").exists())
                .andExpect(jsonPath("$.paths['/api/public/company'].get").exists())
                .andExpect(jsonPath("$.components.schemas.PublicHomeVO.properties.exhibitionIntro.type").value("array"))
                .andExpect(jsonPath("$.components.schemas.PublicHomeVO.properties.samePeriodActivities.type").value("array"))
                .andExpect(jsonPath("$.components.schemas.PublicHomeVO.properties.exhibitorNews.type").value("array"))
                .andExpect(jsonPath("$.components.schemas.PublicHomeVO.properties.exhibitionIntro.items['$ref']").value("#/components/schemas/PublicContentVO"))
                .andExpect(jsonPath("$.components.schemas.PublicContentVO.properties.body.type").value("string"))
                .andExpect(jsonPath("$.components.schemas.PublicContentVO.properties.body.description").value("富文本正文"));
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.CategorySaveRequest.properties.code.description")
                        .value("栏目编码，当前站点内唯一"));
    }

    @Test
    void homeEndpointShouldSanitizeBodyBeforeReturningRichText() throws Exception {
        jdbc.update("UPDATE cms_content SET body = ? WHERE id = 104", "<p>正文</p><script>alert(1)</script><img src=\"https://example.test/scope.png\" onerror=\"alert(1)\">");
        mockMvc.perform(get("/api/public/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.exhibitionScope[0].body").value("<p>正文</p><img src=\"https://example.test/scope.png\">"));
    }
}
