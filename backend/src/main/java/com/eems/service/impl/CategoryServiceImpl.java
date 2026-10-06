package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.eems.common.exception.BusinessException;
import com.eems.dto.CategoryReorderItem;
import com.eems.dto.CategoryReorderRequest;
import com.eems.dto.CategorySaveRequest;
import com.eems.entity.CmsCategory;
import com.eems.mapper.CmsCategoryMapper;
import com.eems.mapper.CmsContentMapper;
import com.eems.service.CategoryService;
import com.eems.vo.CategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;

@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Set<String> MODEL_CODES = Set.of("PAGE", "ARTICLE", "PRODUCT", "CASE", "LINK", "DIRECTORY");

    private final CmsCategoryMapper mapper;
    private final CmsContentMapper contentMapper;

    public CategoryServiceImpl(CmsCategoryMapper mapper, CmsContentMapper contentMapper) {
        this.mapper = mapper;
        this.contentMapper = contentMapper;
    }

    @Override
    public List<CategoryVO> tree() {
        List<CmsCategory> categories = mapper.selectList(new LambdaQueryWrapper<CmsCategory>()
                .orderByAsc(CmsCategory::getParentId)
                .orderByAsc(CmsCategory::getSortOrder)
                .orderByAsc(CmsCategory::getId));
        Map<Long, List<CmsCategory>> childrenByParent = new LinkedHashMap<>();
        for (CmsCategory category : categories) {
            childrenByParent.computeIfAbsent(category.getParentId(), key -> new ArrayList<>()).add(category);
        }
        return childrenByParent.getOrDefault(0L, List.of()).stream()
                .map(category -> toTree(category, childrenByParent))
                .toList();
    }

    @Override
    @Transactional
    public CategoryVO create(CategorySaveRequest request) {
        normalizeAndValidate(request);
        Long parentId = normalizeParentId(request.getParentId());
        ensureParentExists(parentId);
        ensureCodeAvailable(request.getCode().trim(), null);

        CmsCategory value = new CmsCategory();
        copy(value, request, parentId);
        LocalDateTime now = LocalDateTime.now();
        value.setCreatedAt(now);
        value.setUpdatedAt(now);
        value.setDeleted(0);
        if (mapper.insert(value) != 1) {
            throw new BusinessException("CATEGORY_CREATE_FAILED", "栏目创建失败");
        }
        return CategoryVO.from(value, List.of());
    }

    @Override
    @Transactional
    public CategoryVO update(Long id, CategorySaveRequest request) {
        normalizeAndValidate(request);
        CmsCategory value = find(id);
        Long parentId = normalizeParentId(request.getParentId());
        ensureParentExists(parentId);
        ensureCodeAvailable(request.getCode().trim(), id);
        ensureNotDescendant(id, parentId);

        copy(value, request, parentId);
        if (mapper.updateById(value) != 1) {
            throw new BusinessException("CATEGORY_UPDATE_FAILED", "栏目更新失败");
        }
        return CategoryVO.from(value, List.of());
    }

    @Override
    @Transactional
    public CategoryVO delete(Long id) {
        CmsCategory value = find(id);
        long childCount = mapper.selectCount(new LambdaQueryWrapper<CmsCategory>()
                .eq(CmsCategory::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("CATEGORY_HAS_CHILDREN", "栏目存在子栏目，不能删除");
        }
        if (contentMapper.countActiveByCategoryId(id) > 0) {
            throw new BusinessException("CATEGORY_HAS_CONTENT", "栏目存在内容，不能删除");
        }
        if (mapper.deleteById(id) != 1) {
            throw new BusinessException("CATEGORY_DELETE_FAILED", "栏目删除失败");
        }
        return CategoryVO.from(value, List.of());
    }

    @Override
    @Transactional
    public CategoryVO restore(Long id) {
        CmsCategory value = mapper.selectDeletedById(id);
        if (value == null) {
            throw new BusinessException("CATEGORY_NOT_DELETED", "栏目不存在或未被删除");
        }
        ensureParentExists(value.getParentId());
        if (mapper.restoreById(id) != 1) {
            throw new BusinessException("CATEGORY_RESTORE_FAILED", "栏目恢复失败");
        }
        value.setDeleted(0);
        return CategoryVO.from(value, List.of());
    }

    @Override
    @Transactional
    public void reorder(CategoryReorderRequest request) {
        List<CategoryReorderItem> items = request.getItems();
        Set<Long> itemIds = new HashSet<>();
        for (CategoryReorderItem item : items) {
            if (!itemIds.add(item.getId())) {
                throw new BusinessException("CATEGORY_DUPLICATE_REORDER", "排序列表包含重复栏目");
            }
        }

        List<CmsCategory> allCategories = mapper.selectList(null);
        Map<Long, CmsCategory> categoryById = new HashMap<>();
        Map<Long, Long> parentById = new HashMap<>();
        for (CmsCategory category : allCategories) {
            categoryById.put(category.getId(), category);
            parentById.put(category.getId(), normalizeParentId(category.getParentId()));
        }
        for (CategoryReorderItem item : items) {
            if (!categoryById.containsKey(item.getId())) {
                throw new BusinessException("CATEGORY_NOT_FOUND", "栏目不存在");
            }
            Long parentId = normalizeParentId(item.getParentId());
            if (parentId != 0 && !categoryById.containsKey(parentId)) {
                throw new BusinessException("CATEGORY_PARENT_NOT_FOUND", "父栏目不存在");
            }
            parentById.put(item.getId(), parentId);
        }
        validateNoCycle(parentById, itemIds);

        for (CategoryReorderItem item : items) {
            CmsCategory value = categoryById.get(item.getId());
            value.setParentId(normalizeParentId(item.getParentId()));
            value.setSortOrder(item.getSortOrder());
            if (mapper.updateById(value) != 1) {
                throw new BusinessException("CATEGORY_REORDER_FAILED", "栏目排序失败");
            }
        }
    }

    private CategoryVO toTree(CmsCategory category, Map<Long, List<CmsCategory>> childrenByParent) {
        List<CategoryVO> children = childrenByParent.getOrDefault(category.getId(), List.of()).stream()
                .map(value -> toTree(value, childrenByParent))
                .toList();
        return CategoryVO.from(category, children);
    }

    private CmsCategory find(Long id) {
        CmsCategory value = mapper.selectById(id);
        if (value == null) {
            throw new BusinessException("CATEGORY_NOT_FOUND", "栏目不存在");
        }
        return value;
    }

    private void normalizeAndValidate(CategorySaveRequest request) {
        String modelCode = request.getModelCode() == null ? "" : request.getModelCode().trim().toUpperCase();
        if (!MODEL_CODES.contains(modelCode)) {
            throw new BusinessException("CATEGORY_MODEL_INVALID", "栏目模型不正确");
        }
        if (!StringUtils.hasText(request.getName()) || !StringUtils.hasText(request.getCode())) {
            throw new BusinessException("CATEGORY_FIELD_INVALID", "栏目名称和编码不能为空");
        }
        if (request.getIsEnabled() == null || (request.getIsEnabled() != 0 && request.getIsEnabled() != 1)
                || request.getShowInNav() == null || (request.getShowInNav() != 0 && request.getShowInNav() != 1)) {
            throw new BusinessException("CATEGORY_FLAG_INVALID", "栏目启用和导航标记只能为 0 或 1");
        }
        request.setModelCode(modelCode);
    }

    private void copy(CmsCategory value, CategorySaveRequest request, Long parentId) {
        value.setParentId(parentId);
        value.setName(request.getName().trim());
        value.setCode(request.getCode().trim());
        value.setUrlName(trimToNull(request.getUrlName()));
        value.setModelCode(request.getModelCode());
        value.setListTemplate(trimToNull(request.getListTemplate()));
        value.setDetailTemplate(trimToNull(request.getDetailTemplate()));
        value.setSortOrder(request.getSortOrder());
        value.setIsEnabled(request.getIsEnabled());
        value.setShowInNav(request.getShowInNav());
        value.setLinkUrl(trimToNull(request.getLinkUrl()));
    }

    private void ensureParentExists(Long parentId) {
        if (parentId != 0 && mapper.selectById(parentId) == null) {
            throw new BusinessException("CATEGORY_PARENT_NOT_FOUND", "父栏目不存在");
        }
    }

    private void ensureCodeAvailable(String code, Long currentId) {
        CmsCategory existing = mapper.selectIncludingDeletedByCode(code);
        if (existing == null || existing.getId().equals(currentId)) {
            return;
        }
        if (Integer.valueOf(1).equals(existing.getDeleted())) {
            throw new BusinessException("CATEGORY_CODE_DELETED", "栏目编码已被已删除栏目占用，请恢复原栏目或使用新的编码");
        }
        if (!existing.getId().equals(currentId)) {
            throw new BusinessException("CATEGORY_CODE_EXISTS", "栏目编码已存在");
        }
    }

    private void ensureNotDescendant(Long id, Long parentId) {
        if (id.equals(parentId)) {
            throw new BusinessException("CATEGORY_PARENT_INVALID", "栏目不能移动到自身或子孙栏目下");
        }
        if (parentId == 0) {
            return;
        }
        Map<Long, Long> parentById = new HashMap<>();
        for (CmsCategory category : mapper.selectList(null)) {
            parentById.put(category.getId(), normalizeParentId(category.getParentId()));
        }
        Long current = parentId;
        Set<Long> visited = new HashSet<>();
        while (current != null && current != 0 && visited.add(current)) {
            if (id.equals(current)) {
                throw new BusinessException("CATEGORY_PARENT_INVALID", "栏目不能移动到自身或子孙栏目下");
            }
            current = parentById.get(current);
        }
    }

    private void validateNoCycle(Map<Long, Long> parentById, Set<Long> changedIds) {
        for (Long id : changedIds) {
            Set<Long> visited = new HashSet<>();
            Long current = id;
            while (current != null && current != 0) {
                if (!visited.add(current)) {
                    throw new BusinessException("CATEGORY_PARENT_INVALID", "栏目不能移动到自身或子孙栏目下");
                }
                current = parentById.get(current);
            }
        }
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
