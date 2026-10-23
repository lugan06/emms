package com.eems.site;

import com.eems.common.exception.BusinessException;
import com.eems.dto.CategorySaveRequest;
import com.eems.dto.ContentSaveRequest;
import com.eems.dto.ExhibitionSaveRequest;
import com.eems.dto.SiteConfigUpdateRequest;
import com.eems.entity.SiteConfig;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.SiteConfigMapper;
import com.eems.service.CategoryService;
import com.eems.service.ContentService;
import com.eems.service.ExhibitionService;
import com.eems.service.OperationLogService;
import com.eems.service.SiteConfigService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@Sql({"/public-home-fixture.sql", "/site-data-isolation-fixture.sql"})
class SiteServiceAssociationTest {
    @Autowired private ContentService contentService;
    @Autowired private CategoryService categoryService;
    @Autowired private ExhibitionService exhibitionService;
    @Autowired private SiteConfigService siteConfigService;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private AdminUserMapper adminUserMapper;
    @MockitoBean private OperationLogService operationLogService;
    @MockitoBean private SiteConfigMapper siteConfigMapper;

    @BeforeEach
    void selectSiteA() {
        SiteContext.setSiteId(1L);
    }

    @AfterEach
    void clearSite() {
        SiteContext.clear();
    }

    @Test
    void contentCreateAndUpdateRejectCrossSiteCategoryAndExhibition() {
        ContentSaveRequest request = contentRequest();
        request.setCategoryId(1003L);
        assertBusinessCode("CATEGORY_NOT_FOUND", () -> contentService.create(request, "admin"));
        assertBusinessCode("CATEGORY_NOT_FOUND", () -> contentService.update(1001L, request, "admin"));

        request.setCategoryId(2L);
        request.setExhibitionId(202L);
        assertBusinessCode("EXHIBITION_NOT_FOUND", () -> contentService.create(request, "admin"));
        assertBusinessCode("EXHIBITION_NOT_FOUND", () -> contentService.update(1001L, request, "admin"));

        assertEquals("Site A article", jdbc.queryForObject("SELECT title FROM cms_content WHERE id = 1001", String.class));
        assertEquals(2L, jdbc.queryForObject("SELECT category_id FROM cms_content WHERE id = 1001", Long.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cms_content WHERE title = 'New article'", Integer.class));
    }

    @Test
    void categoryCreateAndUpdateRejectCrossSiteParent() {
        CategorySaveRequest request = categoryRequest("new-child");
        request.setParentId(1003L);

        assertBusinessCode("CATEGORY_PARENT_NOT_FOUND", () -> categoryService.create(request));
        assertBusinessCode("CATEGORY_PARENT_NOT_FOUND", () -> categoryService.update(2L, request));

        assertEquals(1L, jdbc.queryForObject("SELECT parent_id FROM cms_category WHERE id = 2", Long.class));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM cms_category WHERE code = 'new-child'", Integer.class));
    }

    @Test
    void siteConfigRejectsCrossSiteCurrentExhibition() {
        SiteConfigUpdateRequest request = new SiteConfigUpdateRequest("A", null, null, null, null,
                null, null, null, null, null, 202L, null);

        assertBusinessCode("EXHIBITION_NOT_FOUND", () -> siteConfigService.updateDefault(request));
        verify(siteConfigMapper, never()).updateById(any(SiteConfig.class));
    }

    @Test
    void currentSiteAssociationsStillSaveSuccessfully() {
        ContentSaveRequest content = contentRequest();
        content.setExhibitionId(101L);
        contentService.update(1001L, content, "admin");
        assertEquals(101L, jdbc.queryForObject("SELECT exhibition_id FROM cms_content WHERE id = 1001", Long.class));

        CategorySaveRequest category = categoryRequest("exhibition-intro");
        category.setParentId(1L);
        categoryService.update(2L, category);
        assertEquals(1L, jdbc.queryForObject("SELECT parent_id FROM cms_category WHERE id = 2", Long.class));

        SiteConfig config = new SiteConfig();
        config.setId(1L);
        config.setSiteId(1L);
        config.setConfigCode("default");
        when(siteConfigMapper.selectOne(any())).thenReturn(config);
        when(siteConfigMapper.updateById(any(SiteConfig.class))).thenReturn(1);
        siteConfigService.updateDefault(new SiteConfigUpdateRequest("A", null, null, null, null,
                null, null, null, null, null, 101L, null));
        assertEquals(101L, config.getCurrentExhibitionId());
    }

    @Test
    void duplicateChecksAllowOtherSiteCodesAndSlugs() {
        ExhibitionSaveRequest exhibition = exhibitionRequest("EXPO-B");
        exhibitionService.update(101L, exhibition, "admin");
        assertEquals("EXPO-B", jdbc.queryForObject("SELECT exhibition_code FROM exhibition WHERE id = 101", String.class));

        CategorySaveRequest category = categoryRequest("only-b");
        categoryService.update(2L, category);
        assertEquals("only-b", jdbc.queryForObject("SELECT code FROM cms_category WHERE id = 2", String.class));

        ContentSaveRequest content = contentRequest();
        content.setSlug("only-b");
        contentService.update(1001L, content, "admin");
        assertEquals("only-b", jdbc.queryForObject("SELECT slug FROM cms_content WHERE id = 1001", String.class));
    }

    @Test
    void duplicateChecksStillRejectCurrentSiteValues() {
        jdbc.update("INSERT INTO exhibition (id, site_id, exhibition_code, title, status, deleted) " +
                "VALUES (102, 1, 'EXPO-A-OTHER', 'Another A exhibition', 'DRAFT', 0)");
        assertBusinessCode("EXHIBITION_CODE_EXISTS",
                () -> exhibitionService.update(101L, exhibitionRequest("EXPO-A-OTHER"), "admin"));
        assertBusinessCode("CATEGORY_CODE_EXISTS",
                () -> categoryService.update(1L, categoryRequest("exhibition-intro")));
        ContentSaveRequest content = contentRequest();
        content.setSlug("shared-slug");
        assertBusinessCode("CONTENT_SLUG_EXISTS", () -> contentService.update(102L, content, "admin"));
    }

    private ContentSaveRequest contentRequest() {
        ContentSaveRequest request = new ContentSaveRequest();
        request.setCategoryId(2L);
        request.setTitle("New article");
        return request;
    }

    private CategorySaveRequest categoryRequest(String code) {
        CategorySaveRequest request = new CategorySaveRequest();
        request.setName("New category");
        request.setCode(code);
        request.setModelCode("ARTICLE");
        return request;
    }

    private ExhibitionSaveRequest exhibitionRequest(String code) {
        ExhibitionSaveRequest request = new ExhibitionSaveRequest();
        request.setExhibitionCode(code);
        request.setTitle("Updated A exhibition");
        request.setYear(2027);
        request.setStartAt(LocalDateTime.of(2027, 1, 1, 9, 0));
        request.setEndAt(LocalDateTime.of(2027, 1, 2, 17, 0));
        return request;
    }

    private void assertBusinessCode(String code, Runnable action) {
        assertEquals(code, assertThrows(BusinessException.class, action::run).getCode());
    }
}
