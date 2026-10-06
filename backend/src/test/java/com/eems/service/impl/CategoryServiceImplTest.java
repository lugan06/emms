package com.eems.service.impl;

import com.eems.common.exception.BusinessException;
import com.eems.dto.CategoryReorderItem;
import com.eems.dto.CategoryReorderRequest;
import com.eems.dto.CategorySaveRequest;
import com.eems.entity.CmsCategory;
import com.eems.mapper.CmsCategoryMapper;
import com.eems.mapper.CmsContentMapper;
import com.eems.vo.CategoryVO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryServiceImplTest {
    private final CmsCategoryMapper mapper = mock(CmsCategoryMapper.class);
    private final CmsContentMapper contentMapper = mock(CmsContentMapper.class);
    private final CategoryServiceImpl service = new CategoryServiceImpl(mapper, contentMapper);

    @Test
    void treeShouldBuildNestedCategories() {
        CmsCategory root = category(1L, 0L, "新闻", "news");
        CmsCategory child = category(2L, 1L, "行业新闻", "industry-news");
        when(mapper.selectList(any())).thenReturn(List.of(root, child));

        var result = service.tree();

        assertEquals(1, result.size());
        assertEquals("新闻", result.get(0).name());
        assertEquals(1, result.get(0).children().size());
        assertEquals("industry-news", result.get(0).children().get(0).code());
    }

    @Test
    void createShouldRejectDuplicateCode() {
        when(mapper.selectIncludingDeletedByCode("news"))
                .thenReturn(category(3L, 0L, "已存在", "news"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(request("news")));

        assertEquals("CATEGORY_CODE_EXISTS", exception.getCode());
        verify(mapper, never()).insert(any(CmsCategory.class));
    }

    @Test
    void createShouldTellUserToRestoreDeletedCategory() {
        CmsCategory deleted = category(4L, 2L, "行业新闻", "industry-news");
        deleted.setDeleted(1);
        when(mapper.selectIncludingDeletedByCode("industry-news")).thenReturn(deleted);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.create(request("industry-news")));

        assertEquals("CATEGORY_CODE_DELETED", exception.getCode());
        verify(mapper, never()).insert(any(CmsCategory.class));
    }

    @Test
    void createShouldInitializeCommonFieldsBeforeInsert() {
        when(mapper.selectById(0L)).thenReturn(null);
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.insert(any(CmsCategory.class))).thenAnswer(invocation -> {
            CmsCategory value = invocation.getArgument(0);
            assertEquals(0, value.getDeleted());
            org.junit.jupiter.api.Assertions.assertNotNull(value.getCreatedAt());
            org.junit.jupiter.api.Assertions.assertNotNull(value.getUpdatedAt());
            assertEquals(value.getCreatedAt(), value.getUpdatedAt());
            return 1;
        });

        service.create(request("new-category"));

        verify(mapper).insert(any(CmsCategory.class));
    }

    @Test
    void updateShouldRejectMovingToDescendant() {
        CmsCategory root = category(1L, 0L, "根栏目", "root");
        CmsCategory child = category(2L, 1L, "子栏目", "child");
        when(mapper.selectById(1L)).thenReturn(root);
        when(mapper.selectById(2L)).thenReturn(child);
        when(mapper.selectOne(any())).thenReturn(null);
        when(mapper.selectList(null)).thenReturn(List.of(root, child));

        CategorySaveRequest request = request("root");
        request.setParentId(2L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.update(1L, request));

        assertEquals("CATEGORY_PARENT_INVALID", exception.getCode());
        verify(mapper, never()).updateById(any(CmsCategory.class));
    }

    @Test
    void deleteShouldRejectCategoryWithChildren() {
        when(mapper.selectById(1L)).thenReturn(category(1L, 0L, "新闻", "news"));
        when(mapper.selectCount(any())).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.delete(1L));

        assertEquals("CATEGORY_HAS_CHILDREN", exception.getCode());
        verify(contentMapper, never()).countActiveByCategoryId(1L);
        verify(mapper, never()).deleteById(1L);
    }

    @Test
    void deleteShouldRejectCategoryWithActiveContent() {
        when(mapper.selectById(1L)).thenReturn(category(1L, 0L, "新闻", "news"));
        when(mapper.selectCount(any())).thenReturn(0L);
        when(contentMapper.countActiveByCategoryId(1L)).thenReturn(1L);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.delete(1L));

        assertEquals("CATEGORY_HAS_CONTENT", exception.getCode());
        verify(mapper, never()).deleteById(1L);
    }

    @Test
    void deleteShouldReturnDeletedCategoryData() {
        CmsCategory category = category(1L, 0L, "新闻", "news");
        when(mapper.selectById(1L)).thenReturn(category);
        when(mapper.selectCount(any())).thenReturn(0L);
        when(contentMapper.countActiveByCategoryId(1L)).thenReturn(0L);
        when(mapper.deleteById(1L)).thenReturn(1);

        CategoryVO result = service.delete(1L);

        assertEquals(1L, result.id());
        assertEquals("news", result.code());
        verify(mapper).deleteById(1L);
    }

    @Test
    void restoreShouldClearLogicalDelete() {
        CmsCategory deleted = category(4L, 2L, "行业新闻", "industry-news");
        deleted.setDeleted(1);
        when(mapper.selectDeletedById(4L)).thenReturn(deleted);
        when(mapper.selectById(2L)).thenReturn(category(2L, 0L, "新闻资讯", "news"));
        when(mapper.restoreById(4L)).thenReturn(1);

        CategoryVO result = service.restore(4L);

        assertEquals(4L, result.id());
        assertEquals("industry-news", result.code());
        verify(mapper).restoreById(4L);
    }

    @Test
    void restoreShouldRejectMissingParent() {
        CmsCategory deleted = category(4L, 2L, "行业新闻", "industry-news");
        deleted.setDeleted(1);
        when(mapper.selectDeletedById(4L)).thenReturn(deleted);
        when(mapper.selectById(2L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.restore(4L));

        assertEquals("CATEGORY_PARENT_NOT_FOUND", exception.getCode());
        verify(mapper, never()).restoreById(4L);
    }

    @Test
    void reorderShouldRejectCycle() {
        CmsCategory root = category(1L, 0L, "根栏目", "root");
        CmsCategory child = category(2L, 1L, "子栏目", "child");
        when(mapper.selectList(null)).thenReturn(List.of(root, child));

        CategoryReorderItem item = new CategoryReorderItem();
        item.setId(1L);
        item.setParentId(2L);
        item.setSortOrder(0);
        CategoryReorderRequest request = new CategoryReorderRequest();
        request.setItems(List.of(item));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.reorder(request));

        assertEquals("CATEGORY_PARENT_INVALID", exception.getCode());
        verify(mapper, never()).updateById(any(CmsCategory.class));
    }

    @Test
    void reorderShouldUpdateParentAndSortOrder() {
        CmsCategory root = category(1L, 0L, "根栏目", "root");
        CmsCategory child = category(2L, 1L, "子栏目", "child");
        when(mapper.selectList(null)).thenReturn(List.of(root, child));
        when(mapper.updateById(child)).thenReturn(1);

        CategoryReorderItem item = new CategoryReorderItem();
        item.setId(2L);
        item.setParentId(0L);
        item.setSortOrder(10);
        CategoryReorderRequest request = new CategoryReorderRequest();
        request.setItems(List.of(item));

        service.reorder(request);

        assertEquals(0L, child.getParentId());
        assertEquals(10, child.getSortOrder());
        verify(mapper).updateById(child);
    }

    private CategorySaveRequest request(String code) {
        CategorySaveRequest request = new CategorySaveRequest();
        request.setName("新闻资讯");
        request.setCode(code);
        request.setModelCode("ARTICLE");
        request.setParentId(0L);
        request.setSortOrder(0);
        request.setIsEnabled(1);
        request.setShowInNav(1);
        return request;
    }

    private CmsCategory category(Long id, Long parentId, String name, String code) {
        CmsCategory value = new CmsCategory();
        value.setId(id);
        value.setParentId(parentId);
        value.setName(name);
        value.setCode(code);
        value.setModelCode("ARTICLE");
        value.setSortOrder(0);
        value.setIsEnabled(1);
        value.setShowInNav(1);
        return value;
    }
}
