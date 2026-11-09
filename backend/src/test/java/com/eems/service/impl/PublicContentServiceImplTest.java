package com.eems.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import com.eems.common.exception.BusinessException;
import com.eems.dto.PublicContentPageQuery;
import com.eems.entity.CmsCategory;
import com.eems.entity.CmsContent;
import com.eems.mapper.CmsCategoryMapper;
import com.eems.mapper.CmsContentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicContentServiceImplTest {
    @BeforeAll
    static void initializeTableMetadata() {
        MapperBuilderAssistant builder = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        TableInfoHelper.initTableInfo(builder, CmsContent.class);
        TableInfoHelper.initTableInfo(builder, CmsCategory.class);
    }

    private final CmsCategoryMapper categoryMapper = mock(CmsCategoryMapper.class);
    private final CmsContentMapper contentMapper = mock(CmsContentMapper.class);
    private final PublicContentServiceImpl service = new PublicContentServiceImpl(categoryMapper, contentMapper);

    @Test
    void categoryTreeShouldBuildOnlyFromEnabledNavigationCategories() {
        CmsCategory root = category(1L, 0L, "新闻资讯", "news", 1, 1);
        CmsCategory child = category(2L, 1L, "展会动态", "exhibition-news", 1, 1);
        when(categoryMapper.selectList(any())).thenReturn(List.of(root, child));

        var result = service.categoryTree();

        assertEquals(1, result.size());
        assertEquals("news", result.get(0).code());
        assertEquals(1, result.get(0).children().size());
    }

    @Test
    void pageShouldReturnOnlyPublishedContentForPublicCategory() {
        CmsCategory category = category(1L, 0L, "新闻", "news", 1, 1);
        CmsContent content = content(10L, 1L, "PUBLISHED");
        Page<CmsContent> page = new Page<>(1, 20);
        page.setRecords(List.of(content));
        page.setTotal(1);
        when(categoryMapper.selectOne(any())).thenReturn(category);
        when(contentMapper.selectPage(any(), any())).thenReturn(page);

        PublicContentPageQuery query = new PublicContentPageQuery();
        query.setKeyword("新闻");

        assertEquals(1, service.pageByCategory("news", query).getTotal());
        verify(contentMapper).selectPage(any(), any());
    }

    @Test
    void subtreePageShouldUseLatestOrderAndKeepActualCategoryMetadata() {
        CmsCategory root = category(1L, 0L, "新闻中心", "news", 1, 1);
        CmsCategory child = category(2L, 1L, "自定义新闻", "custom-news", 1, 0);
        CmsCategory grandchild = category(3L, 2L, "孙栏目", "nested-news", 1, 1);
        CmsCategory unrelated = category(4L, 0L, "其他栏目", "other", 1, 1);
        CmsCategory orphan = category(6L, 5L, "禁用父栏目的子栏目", "hidden", 1, 1);
        when(categoryMapper.selectOne(any())).thenReturn(root);
        when(categoryMapper.selectList(any())).thenReturn(List.of(root, child, grandchild, unrelated, orphan));
        when(contentMapper.selectPage(any(), any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<CmsContent> wrapper = invocation.getArgument(1);
            String sql = wrapper.getSqlSegment();
            assertTrue(sql.contains("category_id IN"));
            assertTrue(sql.contains("published_at DESC,id DESC"));
            assertFalse(sql.contains("is_top"));
            assertFalse(sql.contains("is_recommend"));
            assertFalse(sql.contains("sort_order"));
            assertFalse(sql.contains("exhibition_id"));
            assertTrue(wrapper.getParamNameValuePairs().containsValue(1L));
            assertTrue(wrapper.getParamNameValuePairs().containsValue(2L));
            assertTrue(wrapper.getParamNameValuePairs().containsValue(3L));
            assertFalse(wrapper.getParamNameValuePairs().containsValue(4L));
            assertFalse(wrapper.getParamNameValuePairs().containsValue(6L));
            Page<CmsContent> page = new Page<>(1, 10);
            page.setRecords(List.of(content(10L, 3L, "PUBLISHED")));
            page.setTotal(1);
            return page;
        });
        PublicContentPageQuery query = new PublicContentPageQuery();
        query.setIncludeChildren(true);
        query.setSort("LATEST");

        var result = service.pageByCategory("news", query);

        assertEquals(1, result.getTotal());
        assertEquals("nested-news", result.getRecords().get(0).categoryCode());
        assertEquals("孙栏目", result.getRecords().get(0).categoryName());
    }

    @Test
    void detailShouldRejectUnavailableContent() {
        when(contentMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.get(10L));

        assertEquals("PUBLIC_CONTENT_NOT_FOUND", exception.getCode());
        verify(categoryMapper, never()).selectById(any());
    }

    @Test
    void detailShouldCleanBodyAndRejectDisabledAncestor() {
        CmsContent content = content(10L, 2L, "PUBLISHED");
        content.setBody("<p>正文</p><script>alert('xss')</script>");
        CmsCategory child = category(2L, 1L, "展会动态", "exhibition-news", 1, 1);
        CmsCategory disabledParent = category(1L, 0L, "新闻", "news", 0, 1);
        when(contentMapper.selectOne(any())).thenReturn(content);
        when(categoryMapper.selectById(2L)).thenReturn(child);
        when(categoryMapper.selectById(1L)).thenReturn(disabledParent);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.get(10L));

        assertEquals("PUBLIC_CONTENT_NOT_FOUND", exception.getCode());
    }

    @Test
    void detailShouldCleanPublishedBody() {
        CmsContent content = content(10L, 1L, "PUBLISHED");
        content.setBody("<p>正文</p><img src=\"/uploads/image.png\" alt=\"image.png\" "
                + "onerror=\"alert(1)\"><img src=\"javascript:alert(1)\">"
                + "<img src=\"//evil.example/image.png\"><script>alert('xss')</script>");
        CmsCategory category = category(1L, 0L, "新闻", "news", 1, 1);
        when(contentMapper.selectOne(any())).thenReturn(content);
        when(categoryMapper.selectById(1L)).thenReturn(category);

        var result = service.get(10L);

        assertFalse(result.body().contains("<script"));
        assertTrue(result.body().contains("src=\"/uploads/image.png\""));
        assertFalse(result.body().contains("onerror"));
        assertFalse(result.body().contains("javascript:"));
        assertFalse(result.body().contains("//evil.example"));
        assertEquals("news", result.categoryCode());
    }

    @Test
    void missingHomeCategoryShouldReturnEmptySection() {
        when(categoryMapper.selectOne(any())).thenReturn(null);

        assertEquals(List.of(), service.topByCategory("industry-news", null, 6));
        verify(contentMapper, never()).selectList(any());
    }

    @Test
    void homeSectionShouldFilterByExhibitionInsteadOfContentPrimaryKey() {
        CmsCategory category = category(2L, 0L, "展会简介", "exhibition-intro", 1, 1);
        CmsContent content = content(102L, 2L, "PUBLISHED");
        content.setExhibitionId(1L);
        when(categoryMapper.selectOne(any())).thenReturn(category);
        when(contentMapper.selectList(any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<CmsContent> wrapper = invocation.getArgument(0);
            String sql = wrapper.getSqlSegment();
            assertTrue(sql.contains("exhibition_id ="));
            assertFalse(sql.matches("(?s).*\\bid\\s*=.*"));
            assertTrue(sql.contains("status ="));
            assertTrue(sql.contains("published_at <="));
            assertTrue(sql.endsWith("LIMIT 6"));
            assertTrue(wrapper.getParamNameValuePairs().containsValue(1L));
            return List.of(content);
        });

        var result = service.topByCategory("exhibition-intro", 1L, 6);

        assertEquals(102L, result.get(0).id());
        assertEquals(1L, result.get(0).exhibitionId());
    }

    @Test
    void globalNewsShouldNotFilterByExhibitionOrContentId() {
        when(categoryMapper.selectOne(any())).thenReturn(category(7L, 0L, "行业资讯", "industry-news", 1, 1));
        when(contentMapper.selectList(any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<CmsContent> wrapper = invocation.getArgument(0);
            String sql = wrapper.getSqlSegment();
            assertFalse(sql.contains("exhibition_id"));
            assertFalse(sql.matches("(?s).*\\bid\\s*=.*"));
            return List.of();
        });

        assertEquals(List.of(), service.topByCategory("industry-news", null, 6));
    }

    @Test
    void homeBodyShouldPreserveRichTextAndRemoveExecutableHtml() {
        when(categoryMapper.selectOne(any())).thenReturn(category(2L, 0L, "展会简介", "exhibition-intro", 1, 1));
        CmsContent content = content(102L, 2L, "PUBLISHED");
        content.setBody("<h2>展会亮点</h2><p>完整正文</p><ul><li>参展范围</li></ul>"
                + "<img src=\"/uploads/editor-image.png\" alt=\"展会图片\" onerror=\"alert(1)\">"
                + "<img src=\"https://example.test/exhibition.png\" onerror=\"alert(1)\">"
                + "<script>alert(1)</script><a href=\"javascript:alert(1)\">链接</a>");
        when(contentMapper.selectList(any())).thenReturn(List.of(content));

        String body = service.topByCategory("exhibition-intro", 1L, 6).get(0).body();

        assertTrue(body.contains("<h2>展会亮点</h2>"));
        assertTrue(body.contains("<p>完整正文</p>"));
        assertTrue(body.contains("<li>参展范围</li>"));
        assertTrue(body.contains("https://example.test/exhibition.png"));
        assertTrue(body.contains("src=\"/uploads/editor-image.png\""));
        assertFalse(body.contains("<script"));
        assertFalse(body.contains("onerror"));
        assertFalse(body.contains("javascript:"));
    }

    @Test
    void homeContentWithoutBodyShouldRemainAvailable() {
        when(categoryMapper.selectOne(any())).thenReturn(category(2L, 0L, "展会简介", "exhibition-intro", 1, 1));
        CmsContent content = content(102L, 2L, "PUBLISHED");
        when(contentMapper.selectList(any())).thenReturn(List.of(content));

        var result = service.topByCategory("exhibition-intro", 1L, 6);

        assertEquals(102L, result.get(0).id());
        assertEquals(null, result.get(0).body());
    }

    @Test
    void overviewShouldIncludeCustomAndNestedContentWithoutLimitingItsSize() {
        CmsCategory root = category(1L, 0L, "展会概览", "exhibition-overview", 1, 1);
        CmsCategory price = category(20L, 1L, "展位定价", "exhibition-price", 1, 0);
        CmsCategory nested = category(21L, 20L, "自定义孙栏目", "custom", 1, 1);
        CmsCategory link = category(22L, 1L, "链接", "link", 1, 1);
        link.setModelCode("LINK");
        when(categoryMapper.selectOne(any())).thenReturn(root);
        when(categoryMapper.selectList(any())).thenReturn(List.of(root, price, nested, link));
        CmsContent content = content(99L, 21L, "PUBLISHED");
        content.setBody("<p>完整正文</p><script>alert(1)</script>");
        when(contentMapper.selectList(any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<CmsContent> wrapper = invocation.getArgument(0);
            String sql = wrapper.getSqlSegment();
            assertTrue(sql.contains("category_id IN"));
            assertTrue(sql.contains("exhibition_id ="));
            assertTrue(sql.contains("exhibition_id IS NULL"));
            assertFalse(sql.contains("LIMIT"));
            assertFalse(wrapper.getParamNameValuePairs().containsValue(22L));
            return List.of(content);
        });
        var result = service.overviewSections(8L);
        assertEquals(1, result.size());
        assertEquals("custom", result.get(0).categoryCode());
        assertEquals("<p>完整正文</p>", result.get(0).items().get(0).body());
    }

    @Test
    void overviewWithoutCurrentExhibitionShouldNotQueryContent() {
        assertEquals(List.of(), service.overviewSections(null));
        verify(contentMapper, never()).selectList(any());
    }

    private CmsCategory category(Long id, Long parentId, String name, String code, int enabled, int showInNav) {
        CmsCategory value = new CmsCategory();
        value.setId(id);
        value.setParentId(parentId);
        value.setName(name);
        value.setCode(code);
        value.setModelCode("ARTICLE");
        value.setIsEnabled(enabled);
        value.setShowInNav(showInNav);
        value.setSortOrder(0);
        return value;
    }

    private CmsContent content(Long id, Long categoryId, String status) {
        CmsContent value = new CmsContent();
        value.setId(id);
        value.setCategoryId(categoryId);
        value.setTitle("公开内容");
        value.setStatus(status);
        value.setIsTop(0);
        value.setIsRecommend(1);
        return value;
    }
}
