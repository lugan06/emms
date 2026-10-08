package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eems.common.api.PageResult;
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
import com.eems.service.ContentService;
import com.eems.service.OperationLogService;
import com.eems.vo.ContentVO;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ContentServiceImpl implements ContentService {
    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String OFFLINE = "OFFLINE";
    private static final String MODULE = "CMS_CONTENT";
    private static final Safelist BODY_SAFELIST = Safelist.relaxed()
            .addTags("img")
            .addAttributes("img", "src", "alt", "width", "height", "class")
            .preserveRelativeLinks(false);

    private final CmsContentMapper mapper;
    private final CmsCategoryMapper categoryMapper;
    private final ExhibitionMapper exhibitionMapper;
    private final AdminUserMapper adminUserMapper;
    private final OperationLogService operationLogService;

    public ContentServiceImpl(CmsContentMapper mapper, CmsCategoryMapper categoryMapper,
                              ExhibitionMapper exhibitionMapper, AdminUserMapper adminUserMapper,
                              OperationLogService operationLogService) {
        this.mapper = mapper;
        this.categoryMapper = categoryMapper;
        this.exhibitionMapper = exhibitionMapper;
        this.adminUserMapper = adminUserMapper;
        this.operationLogService = operationLogService;
    }

    @Override
    public PageResult<ContentVO> page(ContentPageQuery query) {
        Page<CmsContent> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<CmsContent> wrapper = new LambdaQueryWrapper<CmsContent>()
                .orderByDesc(CmsContent::getIsTop)
                .orderByDesc(CmsContent::getIsRecommend)
                .orderByAsc(CmsContent::getSortOrder)
                .orderByDesc(CmsContent::getCreatedAt)
                .orderByDesc(CmsContent::getId);
        if (query.getCategoryId() != null) {
            wrapper.eq(CmsContent::getCategoryId, query.getCategoryId());
        }
        if (query.getExhibitionId() != null) {
            wrapper.eq(CmsContent::getExhibitionId, query.getExhibitionId());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(CmsContent::getTitle, query.getKeyword().trim());
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(CmsContent::getStatus, normalizeStatus(query.getStatus()));
        }
        if (query.getIsTop() != null) {
            validateFlag(query.getIsTop(), "isTop");
            wrapper.eq(CmsContent::getIsTop, query.getIsTop());
        }
        if (query.getIsRecommend() != null) {
            validateFlag(query.getIsRecommend(), "isRecommend");
            wrapper.eq(CmsContent::getIsRecommend, query.getIsRecommend());
        }
        Page<CmsContent> result = mapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords().stream().map(ContentVO::from).toList(),
                result.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    public ContentVO get(Long id) {
        return ContentVO.from(find(id));
    }

    @Override
    @Transactional
    public ContentVO create(ContentSaveRequest request, String username) {
        validateRequest(request);
        validateRelations(request);
        ensureSlugAvailable(request.getSlug(), null);
        CmsContent value = new CmsContent();
        copy(value, request);
        value.setStatus(DRAFT);
        value.setViewCount(0);
        Long adminId = adminId(username);
        value.setCreatedBy(adminId);
        value.setUpdatedBy(adminId);
        if (mapper.insert(value) != 1) {
            throw new BusinessException("CONTENT_CREATE_FAILED", "内容创建失败");
        }
        log(username, "新增内容", "/api/admin/contents", value.getId());
        return ContentVO.from(value);
    }

    @Override
    @Transactional
    public ContentVO update(Long id, ContentSaveRequest request, String username) {
        validateRequest(request);
        validateRelations(request);
        CmsContent value = find(id);
        ensureSlugAvailable(request.getSlug(), id);
        copy(value, request);
        value.setUpdatedBy(adminId(username));
        if (mapper.updateById(value) != 1) {
            throw new BusinessException("CONTENT_UPDATE_FAILED", "内容更新失败");
        }
        log(username, "编辑内容", "/api/admin/contents/" + id, id);
        return ContentVO.from(value);
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        find(id);
        if (mapper.deleteById(id) != 1) {
            throw new BusinessException("CONTENT_DELETE_FAILED", "内容删除失败");
        }
        log(username, "删除内容", "/api/admin/contents/" + id, id);
    }

    @Override
    @Transactional
    public ContentVO publish(Long id, String username) {
        CmsContent value = find(id);
        requireStatus(value, DRAFT, "只有草稿内容可以发布", "CONTENT_NOT_DRAFT");
        value.setStatus(PUBLISHED);
        value.setPublishedAt(LocalDateTime.now());
        value.setUpdatedBy(adminId(username));
        updateStatus(value, "CONTENT_PUBLISH_FAILED", "内容发布失败");
        log(username, "发布内容", "/api/admin/contents/" + id + "/publish", id);
        return ContentVO.from(value);
    }

    @Override
    @Transactional
    public ContentVO offline(Long id, String username) {
        CmsContent value = find(id);
        requireStatus(value, PUBLISHED, "只有已发布内容可以下架", "CONTENT_NOT_PUBLISHED");
        value.setStatus(OFFLINE);
        value.setUpdatedBy(adminId(username));
        updateStatus(value, "CONTENT_OFFLINE_FAILED", "内容下架失败");
        log(username, "下架内容", "/api/admin/contents/" + id + "/offline", id);
        return ContentVO.from(value);
    }

    private void validateRequest(ContentSaveRequest request) {
        validateFlag(request.getIsTop(), "isTop");
        validateFlag(request.getIsRecommend(), "isRecommend");
    }

    private void validateRelations(ContentSaveRequest request) {
        CmsCategory category = categoryMapper.selectById(request.getCategoryId());
        if (category == null) {
            throw new BusinessException("CATEGORY_NOT_FOUND", "栏目不存在或已删除");
        }
        if (request.getExhibitionId() != null && exhibitionMapper.selectById(request.getExhibitionId()) == null) {
            throw new BusinessException("EXHIBITION_NOT_FOUND", "关联展会不存在或已删除");
        }
    }

    private void ensureSlugAvailable(String slug, Long currentId) {
        String normalized = trimToNull(slug);
        if (normalized == null) {
            return;
        }
        CmsContent existing = mapper.selectIncludingDeletedBySlug(normalized);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException("CONTENT_SLUG_EXISTS", "内容 URL 标识已存在");
        }
    }

    private void copy(CmsContent value, ContentSaveRequest request) {
        value.setCategoryId(request.getCategoryId());
        value.setExhibitionId(request.getExhibitionId());
        value.setTitle(request.getTitle().trim());
        value.setSlug(trimToNull(request.getSlug()));
        value.setCoverUrl(trimToNull(request.getCoverUrl()));
        value.setSummary(trimToNull(request.getSummary()));
        value.setBody(sanitizeBody(request.getBody()));
        value.setAuthor(trimToNull(request.getAuthor()));
        value.setSource(trimToNull(request.getSource()));
        value.setSortOrder(request.getSortOrder());
        value.setIsTop(request.getIsTop());
        value.setIsRecommend(request.getIsRecommend());
        value.setExtraData(trimToNull(request.getExtraData()));
    }

    private String sanitizeBody(String body) {
        return StringUtils.hasText(body) ? Jsoup.clean(body, BODY_SAFELIST) : null;
    }

    private CmsContent find(Long id) {
        CmsContent value = mapper.selectById(id);
        if (value == null) {
            throw new BusinessException("CONTENT_NOT_FOUND", "内容不存在");
        }
        return value;
    }

    private String normalizeStatus(String status) {
        String normalized = status.trim().toUpperCase();
        if (!DRAFT.equals(normalized) && !PUBLISHED.equals(normalized) && !OFFLINE.equals(normalized)) {
            throw new BusinessException("CONTENT_STATUS_INVALID", "内容状态不正确");
        }
        return normalized;
    }

    private void validateFlag(Integer value, String field) {
        if (value == null || (value != 0 && value != 1)) {
            throw new BusinessException("CONTENT_FLAG_INVALID", field + " 只能为 0 或 1");
        }
    }

    private void requireStatus(CmsContent value, String expected, String message, String code) {
        if (!expected.equals(value.getStatus())) {
            throw new BusinessException(code, message);
        }
    }

    private void updateStatus(CmsContent value, String code, String message) {
        if (mapper.updateById(value) != 1) {
            throw new BusinessException(code, message);
        }
    }

    private Long adminId(String username) {
        AdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username));
        return user == null ? null : user.getId();
    }

    private void log(String username, String operation, String url, Long id) {
        operationLogService.success(MODULE, username, operation, url, "{\"id\":" + id + "}");
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
