package com.eems.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.exception.BusinessException;
import com.eems.dto.ContentPageQuery;
import com.eems.dto.ContentSaveRequest;
import com.eems.entity.AdminUser;
import com.eems.entity.CmsCategory;
import com.eems.entity.CmsContent;
import com.eems.entity.Exhibition;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.CmsCategoryMapper;
import com.eems.mapper.CmsContentMapper;
import com.eems.mapper.ExhibitionMapper;
import com.eems.service.OperationLogService;
import com.eems.vo.ContentVO;
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

class ContentServiceImplTest {
    private final CmsContentMapper mapper = mock(CmsContentMapper.class);
    private final CmsCategoryMapper categoryMapper = mock(CmsCategoryMapper.class);
    private final ExhibitionMapper exhibitionMapper = mock(ExhibitionMapper.class);
    private final AdminUserMapper adminUserMapper = mock(AdminUserMapper.class);
    private final OperationLogService logService = mock(OperationLogService.class);
    private final ContentServiceImpl service = new ContentServiceImpl(
            mapper, categoryMapper, exhibitionMapper, adminUserMapper, logService);

    @Test
    void pageShouldApplyContentFilters() {
        CmsContent content = content(1L, "DRAFT");
        Page<CmsContent> page = new Page<>(1, 20);
        page.setRecords(List.of(content));
        page.setTotal(1);
        when(mapper.selectPage(any(), any())).thenReturn(page);

        ContentPageQuery query = new ContentPageQuery();
        query.setCategoryId(2L);
        query.setExhibitionId(3L);
        query.setKeyword("新闻");
        query.setStatus("draft");
        query.setIsTop(1);
        query.setIsRecommend(0);

        assertEquals(1L, service.page(query).getTotal());
        verify(mapper).selectPage(any(), any());
    }

    @Test
    void createShouldSanitizeBodyAndWriteModuleLog() {
        when(categoryMapper.selectById(2L)).thenReturn(category(2L));
        when(mapper.selectIncludingDeletedBySlug("news")).thenReturn(null);
        AdminUser admin = new AdminUser();
        admin.setId(8L);
        when(adminUserMapper.selectOne(any())).thenReturn(admin);
        when(mapper.insert(any(CmsContent.class))).thenAnswer(invocation -> {
            CmsContent value = invocation.getArgument(0);
            value.setId(10L);
            return 1;
        });

        ContentVO result = service.create(request(), "admin");

        assertEquals(10L, result.id());
        assertFalse(result.body().contains("<script"));
        assertEquals("DRAFT", result.status());
        verify(logService).success("CMS_CONTENT", "admin", "新增内容", "/api/admin/contents", "{\"id\":10}");
    }

    @Test
    void publishShouldOnlyAllowDraftAndSetPublishedAt() {
        CmsContent value = content(1L, "DRAFT");
        when(mapper.selectById(1L)).thenReturn(value);
        when(adminUserMapper.selectOne(any())).thenReturn(null);
        when(mapper.updateById(value)).thenReturn(1);

        ContentVO result = service.publish(1L, "admin");

        assertEquals("PUBLISHED", result.status());
        assertEquals("PUBLISHED", value.getStatus());
        verify(logService).success("CMS_CONTENT", "admin", "发布内容", "/api/admin/contents/1/publish", "{\"id\":1}");
    }

    @Test
    void publishShouldRejectPublishedContent() {
        when(mapper.selectById(1L)).thenReturn(content(1L, "PUBLISHED"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.publish(1L, "admin"));

        assertEquals("CONTENT_NOT_DRAFT", exception.getCode());
        verify(mapper, never()).updateById(any(CmsContent.class));
    }

    @Test
    void offlineShouldOnlyAllowPublishedContent() {
        CmsContent value = content(2L, "PUBLISHED");
        when(mapper.selectById(2L)).thenReturn(value);
        when(adminUserMapper.selectOne(any())).thenReturn(null);
        when(mapper.updateById(value)).thenReturn(1);

        ContentVO result = service.offline(2L, "admin");

        assertEquals("OFFLINE", result.status());
        verify(logService).success("CMS_CONTENT", "admin", "下架内容", "/api/admin/contents/2/offline", "{\"id\":2}");
    }

    @Test
    void deleteShouldUseLogicalDeleteAndWriteOnlyIdToLog() {
        when(mapper.selectById(3L)).thenReturn(content(3L, "DRAFT"));
        when(mapper.deleteById(3L)).thenReturn(1);

        service.delete(3L, "admin");

        verify(mapper).deleteById(3L);
        verify(logService).success("CMS_CONTENT", "admin", "删除内容", "/api/admin/contents/3", "{\"id\":3}");
    }

    @Test
    void createShouldRejectMissingCategory() {
        when(categoryMapper.selectById(2L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(request(), "admin"));

        assertEquals("CATEGORY_NOT_FOUND", exception.getCode());
        verify(mapper, never()).insert(any(CmsContent.class));
    }

    private ContentSaveRequest request() {
        ContentSaveRequest request = new ContentSaveRequest();
        request.setCategoryId(2L);
        request.setTitle("展会新闻");
        request.setSlug("news");
        request.setBody("<p>正文</p><script>alert('xss')</script>");
        request.setSortOrder(0);
        request.setIsTop(0);
        request.setIsRecommend(1);
        return request;
    }

    private CmsContent content(Long id, String status) {
        CmsContent value = new CmsContent();
        value.setId(id);
        value.setCategoryId(2L);
        value.setTitle("展会新闻");
        value.setStatus(status);
        value.setSortOrder(0);
        value.setIsTop(0);
        value.setIsRecommend(0);
        return value;
    }

    private CmsCategory category(Long id) {
        CmsCategory value = new CmsCategory();
        value.setId(id);
        value.setCode("news");
        return value;
    }
}
