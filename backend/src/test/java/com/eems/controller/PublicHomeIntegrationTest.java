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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    @MockitoBean private SiteConfigService siteConfigService;
    @MockitoBean private CompanyProfileService companyProfileService;
    @MockitoBean private ExhibitionService exhibitionService;

    @BeforeEach
    void configureCurrentExhibition() {
        when(siteConfigService.getCurrentExhibitionId()).thenReturn(1L);
        when(siteConfigService.getPublicDefault()).thenReturn(new PublicSiteVO("测试展会", null, null, null, null, null, null, null, null));
        when(companyProfileService.getPublicDefault()).thenReturn(new PublicCompanyVO("测试公司", null, null, null, null, null, null, null));
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
    void newsSubtreeShouldReturnAllPublicNewsInDateOrderWithAccurateTotal() throws Exception {
        prepareNewsTree();
        mockMvc.perform(get("/api/public/categories/news/contents")
                        .param("includeChildren", "true").param("sort", "LATEST").param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(8))
                .andExpect(jsonPath("$.data.records", hasSize(2)))
                .andExpect(jsonPath("$.data.records[0].id").value(10001))
                .andExpect(jsonPath("$.data.records[0].categoryCode").value("news"))
                .andExpect(jsonPath("$.data.records[1].id").value(10002))
                .andExpect(jsonPath("$.data.records[1].categoryCode").value("nested-news"));
        mockMvc.perform(get("/api/public/categories/news/contents")
                        .param("includeChildren", "true").param("sort", "LATEST").param("pageSize", "2").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(8))
                .andExpect(jsonPath("$.data.records[0].id").value(207))
                .andExpect(jsonPath("$.data.records[1].id").value(206));
        mockMvc.perform(get("/api/public/categories/news/contents")
                        .param("includeChildren", "true").param("sort", "LATEST").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records", hasSize(8)))
                .andExpect(jsonPath("$.data.records[7].id").value(10003));
    }

    @Test
    void newsScopeShouldPreserveExactCategoryDefaultsAndKeywordFiltering() throws Exception {
        prepareNewsTree();
        mockMvc.perform(get("/api/public/categories/news/contents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1));
        mockMvc.perform(get("/api/public/categories/news/contents")
                        .param("includeChildren", "true").param("sort", "LATEST").param("keyword", "其他展会"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(206));
    }

    @Test
    void disabledNewsRootShouldNotBePublic() throws Exception {
        prepareNewsTree();
        jdbc.update("UPDATE cms_category SET is_enabled = 0 WHERE id = 1000");
        mockMvc.perform(get("/api/public/categories/news/contents").param("includeChildren", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void overviewShouldDiscoverCustomChildrenAndKeepEveryPublicItemInCategoryOrder() throws Exception {
        jdbc.update("INSERT INTO cms_category (id,parent_id,name,code,model_code,sort_order) VALUES "
                + "(20,1,'展位定价','exhibition-price','ARTICLE',-2),"
                + "(21,20,'自定义孙栏目','custom-overview','PAGE',1),"
                + "(22,1,'链接栏目','overview-link','LINK',2),"
                + "(23,1,'空栏目','empty-overview','PAGE',3),"
                + "(24,1,'禁用栏目','disabled-overview','DIRECTORY',4),"
                + "(25,24,'禁用子树','disabled-child','ARTICLE',5),"
                + "(26,1,'删除栏目','deleted-overview','PAGE',6)");
        jdbc.update("UPDATE cms_category SET is_enabled=0 WHERE id=24");
        jdbc.update("UPDATE cms_category SET deleted=1 WHERE id=26");
        jdbc.update("INSERT INTO cms_category (id,site_id,parent_id,name,code,model_code) VALUES (27,2,1,'其他站点','foreign-overview','PAGE')");
        for (long id = 1100; id < 1108; id++) {
            jdbc.update("INSERT INTO cms_content (id,category_id,exhibition_id,title,body,status,published_at) VALUES (?,20,1,?,'<p>完整定价</p><script>x()</script>','PUBLISHED','2020-01-01 00:00:00')", id, "定价 " + id);
        }
        jdbc.update("INSERT INTO cms_content (id,category_id,exhibition_id,title,status,published_at) VALUES "
                + "(1200,21,NULL,'全站通用信息','PUBLISHED','2020-01-01 00:00:00'),"
                + "(1201,20,2,'其他展会','PUBLISHED','2020-01-01 00:00:00'),"
                + "(1202,20,1,'草稿','DRAFT',NULL),"
                + "(1203,20,1,'未来','PUBLISHED','2099-01-01 00:00:00'),"
                + "(1204,22,1,'链接内容','PUBLISHED','2020-01-01 00:00:00'),"
                + "(1205,25,1,'禁用子树内容','PUBLISHED','2020-01-01 00:00:00'),"
                + "(1206,26,1,'删除栏目内容','PUBLISHED','2020-01-01 00:00:00'),"
                + "(1207,20,1,'下架','OFFLINE','2020-01-01 00:00:00'),"
                + "(1208,20,1,'删除内容','PUBLISHED','2020-01-01 00:00:00')");
        jdbc.update("UPDATE cms_content SET deleted=1 WHERE id=1208");
        jdbc.update("INSERT INTO cms_content (id,site_id,category_id,exhibition_id,title,status,published_at) VALUES (1209,2,27,1,'跨站点内容','PUBLISHED','2020-01-01 00:00:00')");
        mockMvc.perform(get("/api/public/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overviewSections", hasSize(4)))
                .andExpect(jsonPath("$.data.overviewSections[0].categoryCode").value("exhibition-price"))
                .andExpect(jsonPath("$.data.overviewSections[0].items", hasSize(8)))
                .andExpect(jsonPath("$.data.overviewSections[0].items[0].id").value(1107))
                .andExpect(jsonPath("$.data.overviewSections[0].items[0].body").value("<p>完整定价</p>"))
                .andExpect(jsonPath("$.data.overviewSections[3].categoryCode").value("custom-overview"))
                .andExpect(jsonPath("$.data.overviewSections[3].items[0].id").value(1200));
    }

    @Test
    void overviewShouldBeEmptyWhenCurrentExhibitionIsUnavailable() throws Exception {
        when(siteConfigService.getCurrentExhibitionId()).thenReturn(null);
        mockMvc.perform(get("/api/public/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overviewSections", hasSize(0)));
    }

    private void prepareNewsTree() {
        jdbc.update("INSERT INTO cms_category (id, parent_id, name, code, model_code) VALUES (1000, 0, '新闻中心', 'news', 'DIRECTORY')");
        jdbc.update("UPDATE cms_category SET parent_id = 1000 WHERE id IN (6, 7, 8)");
        jdbc.update("INSERT INTO cms_category (id, parent_id, name, code, is_enabled, show_in_nav, deleted) VALUES "
                + "(1001, 1000, '禁用', 'disabled-news', 0, 1, 0),"
                + "(1002, 1001, '禁用子树', 'hidden-news', 1, 1, 0),"
                + "(1003, 7, '孙栏目', 'nested-news', 1, 0, 0),"
                + "(1004, 0, '其他栏目', 'unrelated', 1, 1, 0),"
                + "(1005, 1000, '已删除栏目', 'deleted-news', 1, 1, 1),"
                + "(1006, 1005, '删除子树', 'deleted-child', 1, 1, 0)");
        jdbc.update("INSERT INTO cms_category (id, site_id, parent_id, name, code) VALUES (1007, 2, 1000, '其他站点', 'foreign-news')");
        jdbc.update("INSERT INTO cms_content (id, category_id, title, status, published_at, is_top, is_recommend, sort_order, deleted) VALUES "
                + "(10001, 1000, '父栏目新闻', 'PUBLISHED', '2025-01-01 00:00:00', 0, 0, 99, 0),"
                + "(10002, 1003, '孙栏目新闻', 'PUBLISHED', '2024-01-01 00:00:00', 0, 0, 99, 0),"
                + "(10003, 6, '旧置顶新闻', 'PUBLISHED', '2010-01-01 00:00:00', 1, 1, -99, 0),"
                + "(10004, 1001, '禁用栏目新闻', 'PUBLISHED', '2025-02-01 00:00:00', 0, 0, 0, 0),"
                + "(10005, 1002, '禁用子树新闻', 'PUBLISHED', '2025-02-01 00:00:00', 0, 0, 0, 0),"
                + "(10006, 1004, '其他栏目新闻', 'PUBLISHED', '2025-02-01 00:00:00', 0, 0, 0, 0),"
                + "(10007, 1006, '删除子树新闻', 'PUBLISHED', '2025-02-01 00:00:00', 0, 0, 0, 0),"
                + "(10008, 6, '草稿新闻', 'DRAFT', '2025-02-01 00:00:00', 0, 0, 0, 0),"
                + "(10009, 6, '未来新闻', 'PUBLISHED', '2099-01-01 00:00:00', 0, 0, 0, 0),"
                + "(10010, 6, '删除新闻', 'PUBLISHED', '2025-02-01 00:00:00', 0, 0, 0, 1),"
                + "(10011, 6, '下架新闻', 'OFFLINE', '2025-02-01 00:00:00', 0, 0, 0, 0)");
        jdbc.update("INSERT INTO cms_content (id, site_id, category_id, title, status, published_at) VALUES "
                + "(10012, 2, 1007, '跨站点新闻', 'PUBLISHED', '2025-02-01 00:00:00')");
    }

    @Test
    void openApiShouldStillDescribeHomeSectionsAndCompanyEndpoint() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value("3.0.1"))
                .andExpect(jsonPath("$.paths['/api/public/home'].get").exists())
                .andExpect(jsonPath("$.paths['/api/public/company'].get").exists())
                .andExpect(jsonPath("$.paths['/api/public/categories/{code}/contents'].get.parameters[?(@.name == 'includeChildren')].schema.type", hasItem("boolean")))
                .andExpect(jsonPath("$.paths['/api/public/categories/{code}/contents'].get.parameters[?(@.name == 'sort')].schema.default", hasItem("DEFAULT")))
                .andExpect(jsonPath("$.components.schemas.PublicHomeVO.properties.exhibitionIntro.type").value("array"))
                .andExpect(jsonPath("$.components.schemas.PublicHomeVO.properties.overviewSections.items['$ref']").value("#/components/schemas/PublicHomeSectionVO"))
                .andExpect(jsonPath("$.components.schemas.PublicHomeVO.properties.samePeriodActivities.type").value("array"))
                .andExpect(jsonPath("$.components.schemas.PublicHomeVO.properties.exhibitorNews.type").value("array"))
                .andExpect(jsonPath("$.components.schemas.PublicHomeVO.properties.exhibitionIntro.items['$ref']").value("#/components/schemas/PublicContentVO"))
                .andExpect(jsonPath("$.components.schemas.PublicContentVO.properties.body.type").value("string"))
                .andExpect(jsonPath("$.components.schemas.PublicContentVO.properties.body.description").value("富文本正文"))
                .andExpect(jsonPath("$.components.schemas.PublicSiteVO.properties.heroVideoUrl.description")
                        .value("首页宣传视频地址，仅返回当前站点的视频地址"))
                .andExpect(jsonPath("$.components.schemas.SiteConfigVO.properties.heroVideoUrl.description")
                        .value("首页宣传视频地址，仅允许当前站点上传的 MP4 地址"))
                .andExpect(jsonPath("$.components.schemas.SiteConfigUpdateRequest.properties.heroVideoUrl.description")
                        .value("首页宣传视频地址，必须为当前站点上传的 MP4 地址"))
                .andExpect(jsonPath("$.paths['/api/admin/files'].post.responses['400'].content['*/*'].schema['$ref']")
                        .value("#/components/schemas/ResultVoid"))
                .andExpect(jsonPath("$.components.schemas.ResultVoid.properties.data.nullable").value(true));
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
