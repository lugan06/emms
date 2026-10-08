package com.eems.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.exception.BusinessException;
import com.eems.dto.PublicContentPageQuery;
import com.eems.entity.CmsCategory;
import com.eems.entity.CmsContent;
import com.eems.mapper.CmsCategoryMapper;
import com.eems.mapper.CmsContentMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicContentServiceImplTest {
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
        content.setBody("<p>正文</p><script>alert('xss')</script>");
        CmsCategory category = category(1L, 0L, "新闻", "news", 1, 1);
        when(contentMapper.selectOne(any())).thenReturn(content);
        when(categoryMapper.selectById(1L)).thenReturn(category);

        var result = service.get(10L);

        assertFalse(result.body().contains("<script"));
        assertEquals("news", result.categoryCode());
    }

    @Test
    void missingHomeCategoryShouldReturnEmptySection() {
        when(categoryMapper.selectOne(any())).thenReturn(null);

        assertEquals(List.of(), service.topByCategory("industry-news", null, 6));
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
