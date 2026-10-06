package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.eems.common.api.PageResult;
import com.eems.common.exception.BusinessException;
import com.eems.dto.ExhibitionPageQuery;
import com.eems.dto.ExhibitionSaveRequest;
import com.eems.dto.PublicExhibitionPageQuery;
import com.eems.entity.Exhibition;
import com.eems.entity.SiteConfig;
import com.eems.mapper.ExhibitionMapper;
import com.eems.mapper.AdminUserMapper;
import com.eems.mapper.SiteConfigMapper;
import com.eems.service.ExhibitionService;
import com.eems.service.OperationLogService;
import com.eems.vo.ExhibitionVO;
import com.eems.vo.PublicExhibitionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class ExhibitionServiceImpl implements ExhibitionService {
    private static final String DRAFT = "DRAFT";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String OFFLINE = "OFFLINE";
    private final ExhibitionMapper mapper;
    private final SiteConfigMapper siteConfigMapper;
    private final OperationLogService operationLogService;
    private final AdminUserMapper adminUserMapper;
    private final ObjectMapper objectMapper;

    public ExhibitionServiceImpl(ExhibitionMapper mapper, SiteConfigMapper siteConfigMapper,
                                  OperationLogService operationLogService, AdminUserMapper adminUserMapper,
                                  ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.siteConfigMapper = siteConfigMapper;
        this.operationLogService = operationLogService;
        this.adminUserMapper = adminUserMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public PageResult<ExhibitionVO> page(ExhibitionPageQuery query) {
        Page<Exhibition> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Exhibition> wrapper = new LambdaQueryWrapper<Exhibition>()
                .orderByDesc(Exhibition::getStartAt).orderByDesc(Exhibition::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Exhibition::getTitle, keyword)
                    .or().like(Exhibition::getExhibitionCode, keyword)
                    .or().like(Exhibition::getVenue, keyword));
        }
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(Exhibition::getStatus, query.getStatus().trim().toUpperCase());
        }
        if (query.getYear() != null) {
            wrapper.eq(Exhibition::getYear, query.getYear());
        }
        Page<Exhibition> result = mapper.selectPage(page, wrapper);
        return new PageResult<>(result.getRecords().stream().map(ExhibitionVO::from).toList(),
                result.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    public PageResult<PublicExhibitionVO> publicPage(PublicExhibitionPageQuery query) {
        LocalDateTime now = LocalDateTime.now();
        Page<Exhibition> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<Exhibition> wrapper = publicWrapper(now)
                .orderByAsc(Exhibition::getStartAt).orderByAsc(Exhibition::getHomeSort)
                .orderByDesc(Exhibition::getId);
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Exhibition::getTitle, keyword)
                    .or().like(Exhibition::getExhibitionCode, keyword)
                    .or().like(Exhibition::getVenue, keyword));
        }
        if (query.getYear() != null) {
            wrapper.eq(Exhibition::getYear, query.getYear());
        }
        Page<Exhibition> result = mapper.selectPage(page, wrapper);
        Long currentId = currentExhibitionId();
        return new PageResult<>(result.getRecords().stream()
                        .map(value -> PublicExhibitionVO.from(value, value.getId().equals(currentId)))
                        .toList(),
                result.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    public ExhibitionVO get(Long id) {
        return ExhibitionVO.from(find(id));
    }

    @Override
    public PublicExhibitionVO publicGet(Long id) {
        Exhibition value = mapper.selectOne(publicWrapper(LocalDateTime.now())
                .eq(Exhibition::getId, id));
        if (value == null) {
            throw new BusinessException("PUBLIC_EXHIBITION_NOT_FOUND", "公开展会不存在");
        }
        return PublicExhibitionVO.from(value, value.getId().equals(currentExhibitionId()));
    }

    @Override
    @Transactional
    public ExhibitionVO create(ExhibitionSaveRequest request, String username) {
        validateTime(request);
        ensureCodeAvailable(request.getExhibitionCode(), null);
        Exhibition value = new Exhibition();
        copy(value, request);
        value.setStatus(DRAFT);
        value.setCreatedBy(adminId(username));
        value.setUpdatedBy(adminId(username));
        if (mapper.insert(value) != 1) {
            throw new BusinessException("EXHIBITION_CREATE_FAILED", "展会创建失败");
        }
        return ExhibitionVO.from(value);
    }

    @Override
    @Transactional
    public ExhibitionVO update(Long id, ExhibitionSaveRequest request, String username) {
        validateTime(request);
        Exhibition value = find(id);
        ensureCodeAvailable(request.getExhibitionCode(), id);
        copy(value, request);
        value.setUpdatedBy(adminId(username));
        if (mapper.updateById(value) != 1) {
            throw new BusinessException("EXHIBITION_UPDATE_FAILED", "展会更新失败");
        }
        return ExhibitionVO.from(value);
    }

    @Override
    @Transactional
    public void delete(Long id, String username) {
        Exhibition value = find(id);
        if (mapper.deleteById(id) != 1) {
            throw new BusinessException("EXHIBITION_DELETE_FAILED", "展会删除失败");
        }
        operationLogService.success(username, "删除展会", "/api/admin/exhibitions/" + id,
                "{\"id\":" + id + ",\"code\":\"" + safe(value.getExhibitionCode()) + "\"}");
    }

    @Override
    @Transactional
    public ExhibitionVO restore(Long id, String username) {
        Long adminId = adminId(username);
        if (mapper.restoreById(id, adminId) != 1) {
            throw new BusinessException("EXHIBITION_NOT_DELETED", "展会不存在或未被删除");
        }
        Exhibition value = find(id);
        operationLogService.success(username, "恢复展会", "/api/admin/exhibitions/" + id + "/restore",
                "{\"id\":" + id + "}");
        return ExhibitionVO.from(value);
    }

    @Override
    @Transactional
    public ExhibitionVO publish(Long id, String username) {
        Exhibition value = find(id);
        requireStatus(value, DRAFT, "只有草稿展会可以发布", "EXHIBITION_NOT_DRAFT");
        value.setStatus(PUBLISHED);
        value.setPublishedAt(LocalDateTime.now());
        value.setUpdatedBy(adminId(username));
        mapper.updateById(value);
        operationLogService.success(username, "发布展会", "/api/admin/exhibitions/" + id + "/publish",
                "{\"id\":" + id + "}");
        return ExhibitionVO.from(value);
    }

    @Override
    @Transactional
    public ExhibitionVO republish(Long id, String username) {
        Exhibition value = find(id);
        requireStatus(value, OFFLINE, "只有已下架展会可以重新上架", "EXHIBITION_NOT_OFFLINE");
        value.setStatus(PUBLISHED);
        value.setPublishedAt(LocalDateTime.now());
        value.setUpdatedBy(adminId(username));
        if (mapper.updateById(value) != 1) {
            throw new BusinessException("EXHIBITION_REPUBLISH_FAILED", "展会重新上架失败");
        }
        operationLogService.success(username, "重新上架展会", "/api/admin/exhibitions/" + id + "/republish",
                "{\"id\":" + id + "}");
        return ExhibitionVO.from(value);
    }

    @Override
    @Transactional
    public ExhibitionVO offline(Long id, String username) {
        Exhibition value = find(id);
        requireStatus(value, PUBLISHED, "只有已发布展会可以下架", "EXHIBITION_NOT_PUBLISHED");
        value.setStatus(OFFLINE);
        value.setUpdatedBy(adminId(username));
        mapper.updateById(value);
        operationLogService.success(username, "下架展会", "/api/admin/exhibitions/" + id + "/offline",
                "{\"id\":" + id + "}");
        return ExhibitionVO.from(value);
    }

    @Override
    @Transactional
    public ExhibitionVO setCurrent(Long id, String username) {
        Exhibition value = find(id);
        requireStatus(value, PUBLISHED, "只有已发布展会可以设置为当前展会", "EXHIBITION_NOT_PUBLISHED");
        SiteConfig config = siteConfigMapper.selectOne(new LambdaQueryWrapper<SiteConfig>()
                .eq(SiteConfig::getConfigCode, "default"));
        if (config == null) {
            throw new BusinessException("SITE_CONFIG_NOT_FOUND", "默认站点配置不存在");
        }
        config.setCurrentExhibitionId(id);
        if (siteConfigMapper.updateById(config) != 1) {
            throw new BusinessException("CURRENT_EXHIBITION_UPDATE_FAILED", "当前展会设置失败");
        }
        operationLogService.success(username, "设置当前展会", "/api/admin/exhibitions/" + id + "/set-current",
                "{\"id\":" + id + "}");
        return ExhibitionVO.from(value);
    }

    private Exhibition find(Long id) {
        Exhibition value = mapper.selectById(id);
        if (value == null) {
            throw new BusinessException("EXHIBITION_NOT_FOUND", "展会不存在");
        }
        return value;
    }

    private LambdaQueryWrapper<Exhibition> publicWrapper(LocalDateTime now) {
        return new LambdaQueryWrapper<Exhibition>()
                .eq(Exhibition::getStatus, PUBLISHED)
                .and(w -> w.isNull(Exhibition::getPublishedAt)
                        .or().le(Exhibition::getPublishedAt, now));
    }

    private Long currentExhibitionId() {
        SiteConfig config = siteConfigMapper.selectOne(new LambdaQueryWrapper<SiteConfig>()
                .eq(SiteConfig::getConfigCode, "default"));
        return config == null ? null : config.getCurrentExhibitionId();
    }

    private void validateTime(ExhibitionSaveRequest request) {
        if (request.getStartAt() == null || request.getEndAt() == null
                || !request.getStartAt().isBefore(request.getEndAt())) {
            throw new BusinessException("EXHIBITION_TIME_INVALID", "开始时间必须早于结束时间");
        }
    }

    private void ensureCodeAvailable(String code, Long currentId) {
        Exhibition existing = mapper.selectOne(new LambdaQueryWrapper<Exhibition>()
                .eq(Exhibition::getExhibitionCode, code));
        if (existing != null && !existing.getId().equals(currentId)) {
            throw new BusinessException("EXHIBITION_CODE_EXISTS", "展会编码已存在");
        }
    }

    private void copy(Exhibition value, ExhibitionSaveRequest request) {
        value.setExhibitionCode(request.getExhibitionCode()); value.setTitle(request.getTitle());
        value.setSubtitle(request.getSubtitle()); value.setYear(request.getYear()); value.setEdition(request.getEdition());
        value.setCoverUrl(request.getCoverUrl()); value.setSummary(request.getSummary()); value.setDescription(request.getDescription());
        value.setVenue(request.getVenue()); value.setAddress(request.getAddress()); value.setStartAt(request.getStartAt());
        value.setEndAt(request.getEndAt()); value.setRegistrationUrl(request.getRegistrationUrl()); value.setContactName(request.getContactName());
        value.setContactPhone(request.getContactPhone()); value.setShowOnHome(request.getShowOnHome()); value.setHomeSort(request.getHomeSort());
        value.setExtraData(normalizeExtraData(request.getExtraData()));
    }

    private String normalizeExtraData(String extraData) {
        if (!StringUtils.hasText(extraData)) {
            return null;
        }
        String value = extraData.trim();
        try {
            objectMapper.readTree(value);
            return value;
        } catch (JsonProcessingException exception) {
            try {
                return objectMapper.writeValueAsString(extraData);
            } catch (JsonProcessingException serializationException) {
                throw new BusinessException("EXHIBITION_EXTRA_DATA_INVALID", "扩展属性格式不正确");
            }
        }
    }

    private void requireStatus(Exhibition value, String expected, String message, String code) {
        if (!expected.equals(value.getStatus())) {
            throw new BusinessException(code, message);
        }
    }

    private Long adminId(String username) {
        com.eems.entity.AdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<com.eems.entity.AdminUser>()
                .eq(com.eems.entity.AdminUser::getUsername, username));
        return user == null ? null : user.getId();
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
