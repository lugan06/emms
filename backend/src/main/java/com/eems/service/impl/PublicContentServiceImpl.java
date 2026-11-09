package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.api.PageResult;
import com.eems.common.exception.BusinessException;
import com.eems.common.util.RichTextSanitizer;
import com.eems.dto.PublicContentPageQuery;
import com.eems.entity.CmsCategory;
import com.eems.entity.CmsContent;
import com.eems.mapper.CmsCategoryMapper;
import com.eems.mapper.CmsContentMapper;
import com.eems.service.PublicContentService;
import com.eems.vo.PublicCategoryVO;
import com.eems.vo.PublicContentListVO;
import com.eems.vo.PublicContentVO;
import com.eems.vo.PublicHomeSectionVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublicContentServiceImpl implements PublicContentService {
    private static final String PUBLISHED = "PUBLISHED";
    private final CmsCategoryMapper categoryMapper;
    private final CmsContentMapper contentMapper;

    public PublicContentServiceImpl(CmsCategoryMapper categoryMapper, CmsContentMapper contentMapper) {
        this.categoryMapper = categoryMapper;
        this.contentMapper = contentMapper;
    }

    @Override
    public List<PublicCategoryVO> categoryTree() {
        List<CmsCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<CmsCategory>()
                .eq(CmsCategory::getIsEnabled, 1)
                .eq(CmsCategory::getShowInNav, 1)
                .orderByAsc(CmsCategory::getParentId)
                .orderByAsc(CmsCategory::getSortOrder)
                .orderByAsc(CmsCategory::getId));
        Map<Long, List<CmsCategory>> childrenByParent = new LinkedHashMap<>();
        for (CmsCategory category : categories) {
            childrenByParent.computeIfAbsent(category.getParentId(), key -> new ArrayList<>()).add(category);
        }
        return childrenByParent.getOrDefault(0L, List.of()).stream()
                .map(category -> toCategoryTree(category, childrenByParent))
                .toList();
    }

    @Override
    public PageResult<PublicContentListVO> pageByCategory(String code, PublicContentPageQuery query) {
        CmsCategory category = findPublicCategory(code);
        Map<Long, CmsCategory> categories = categoryScope(category, query.isIncludeChildren());
        Page<CmsContent> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<CmsContent> wrapper = publicContentWrapper(null, null)
                .in(CmsContent::getCategoryId, categories.keySet());
        if (!"LATEST".equals(query.getSort())) {
            wrapper.orderByDesc(CmsContent::getIsTop)
                    .orderByDesc(CmsContent::getIsRecommend)
                    .orderByAsc(CmsContent::getSortOrder);
        }
        wrapper.orderByDesc(CmsContent::getPublishedAt).orderByDesc(CmsContent::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(CmsContent::getTitle, query.getKeyword().trim());
        }
        Page<CmsContent> result = contentMapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords().stream()
                        .map(value -> PublicContentListVO.from(value, categories.get(value.getCategoryId())))
                        .toList(), result.getTotal(), query.getPage(), query.getPageSize());
    }

    private Map<Long, CmsCategory> categoryScope(CmsCategory root, boolean includeChildren) {
        Map<Long, CmsCategory> result = new LinkedHashMap<>();
        result.put(root.getId(), root);
        if (!includeChildren) return result;
        // The mapper retains site isolation and logical-delete filtering.
        Map<Long, List<CmsCategory>> children = new LinkedHashMap<>();
        for (CmsCategory category : categoryMapper.selectList(new LambdaQueryWrapper<CmsCategory>()
                .eq(CmsCategory::getIsEnabled, 1)
                .orderByAsc(CmsCategory::getSortOrder).orderByAsc(CmsCategory::getId))) {
            children.computeIfAbsent(category.getParentId(), key -> new ArrayList<>()).add(category);
        }
        ArrayDeque<Long> pending = new ArrayDeque<>();
        pending.add(root.getId());
        while (!pending.isEmpty()) {
            for (CmsCategory child : children.getOrDefault(pending.removeFirst(), List.of())) {
                if (!result.containsKey(child.getId())) {
                    result.put(child.getId(), child);
                    pending.addLast(child.getId());
                }
            }
        }
        return result;
    }

    @Override
    public PublicContentVO get(Long id) {
        CmsContent value = contentMapper.selectOne(publicContentWrapper(null, id));
        if (value == null) {
            throw new BusinessException("PUBLIC_CONTENT_NOT_FOUND", "公开内容不存在");
        }
        CmsCategory category = findPublicCategoryById(value.getCategoryId());
        return PublicContentVO.from(value, category, safeBody(value.getBody()));
    }

    @Override
    public List<PublicContentVO> topByCategory(String code, Long exhibitionId, int limit) {
        if (limit <= 0) {
            return List.of();
        }
        CmsCategory category = findPublicCategoryOrNull(code);
        if (category == null) {
            return List.of();
        }
        LambdaQueryWrapper<CmsContent> wrapper = publicContentWrapper(category.getId(), null);
        if (exhibitionId != null) {
            wrapper.eq(CmsContent::getExhibitionId, exhibitionId);
        }
        List<CmsContent> values = contentMapper.selectList(wrapper
                .orderByDesc(CmsContent::getIsTop)
                .orderByDesc(CmsContent::getIsRecommend)
                .orderByAsc(CmsContent::getSortOrder)
                .orderByDesc(CmsContent::getPublishedAt)
                .orderByDesc(CmsContent::getId)
                .last("LIMIT " + Math.min(limit, 100)));
        return values.stream().map(value -> PublicContentVO.from(value, category, safeBody(value.getBody()))).toList();
    }

    @Override
    public List<PublicHomeSectionVO> overviewSections(Long exhibitionId) {
        if (exhibitionId == null) return List.of();
        CmsCategory root = findPublicCategoryOrNull("exhibition-overview");
        if (root == null) return List.of();
        Map<Long, CmsCategory> scope = categoryScope(root, true);
        List<CmsCategory> categories = scope.values().stream()
                .filter(category -> !category.getId().equals(root.getId()))
                .filter(category -> "PAGE".equals(category.getModelCode()) || "ARTICLE".equals(category.getModelCode()))
                .toList();
        if (categories.isEmpty()) return List.of();
        List<CmsContent> values = contentMapper.selectList(publicContentWrapper(null, null)
                .in(CmsContent::getCategoryId, categories.stream().map(CmsCategory::getId).toList())
                .and(query -> query.eq(CmsContent::getExhibitionId, exhibitionId).or().isNull(CmsContent::getExhibitionId))
                .orderByDesc(CmsContent::getIsTop).orderByDesc(CmsContent::getIsRecommend)
                .orderByAsc(CmsContent::getSortOrder).orderByDesc(CmsContent::getPublishedAt).orderByDesc(CmsContent::getId));
        Map<Long, List<PublicContentVO>> items = new LinkedHashMap<>();
        for (CmsContent value : values) {
            items.computeIfAbsent(value.getCategoryId(), key -> new ArrayList<>())
                    .add(PublicContentVO.from(value, scope.get(value.getCategoryId()), safeBody(value.getBody())));
        }
        return categories.stream().filter(category -> items.containsKey(category.getId()))
                .map(category -> new PublicHomeSectionVO(category.getId(), category.getCode(), category.getName(), items.get(category.getId())))
                .toList();
    }

    private PublicCategoryVO toCategoryTree(CmsCategory category, Map<Long, List<CmsCategory>> childrenByParent) {
        List<PublicCategoryVO> children = childrenByParent.getOrDefault(category.getId(), List.of()).stream()
                .map(value -> toCategoryTree(value, childrenByParent))
                .toList();
        return PublicCategoryVO.from(category, children);
    }

    private CmsCategory findPublicCategory(String code) {
        CmsCategory category = findPublicCategoryOrNull(code);
        if (category == null) {
            throw new BusinessException("PUBLIC_CATEGORY_NOT_FOUND", "公开栏目不存在");
        }
        return category;
    }

    private CmsCategory findPublicCategoryOrNull(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        CmsCategory category = categoryMapper.selectOne(new LambdaQueryWrapper<CmsCategory>()
                .eq(CmsCategory::getCode, code.trim())
                .eq(CmsCategory::getIsEnabled, 1));
        if (category == null || !ancestorsEnabled(category)) {
            return null;
        }
        return category;
    }

    private CmsCategory findPublicCategoryById(Long id) {
        CmsCategory category = categoryMapper.selectById(id);
        if (category == null || !Integer.valueOf(1).equals(category.getIsEnabled()) || !ancestorsEnabled(category)) {
            throw new BusinessException("PUBLIC_CONTENT_NOT_FOUND", "公开内容不存在");
        }
        return category;
    }

    private boolean ancestorsEnabled(CmsCategory category) {
        Long parentId = category.getParentId();
        int guard = 0;
        while (parentId != null && parentId != 0 && guard++ < 100) {
            CmsCategory parent = categoryMapper.selectById(parentId);
            if (parent == null || !Integer.valueOf(1).equals(parent.getIsEnabled())) {
                return false;
            }
            parentId = parent.getParentId();
        }
        return guard < 100;
    }

    private LambdaQueryWrapper<CmsContent> publicContentWrapper(Long categoryId, Long contentId) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CmsContent> wrapper = new LambdaQueryWrapper<CmsContent>()
                .eq(CmsContent::getStatus, PUBLISHED)
                .and(value -> value.isNull(CmsContent::getPublishedAt)
                        .or().le(CmsContent::getPublishedAt, now));
        if (categoryId != null) {
            wrapper.eq(CmsContent::getCategoryId, categoryId);
        }
        if (contentId != null) {
            wrapper.eq(CmsContent::getId, contentId);
        }
        return wrapper;
    }

    private String safeBody(String body) {
        return RichTextSanitizer.sanitize(body);
    }
}
