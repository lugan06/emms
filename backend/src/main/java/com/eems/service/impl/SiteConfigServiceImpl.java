package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eems.common.exception.BusinessException;
import com.eems.config.FileStorageProperties;
import com.eems.dto.SiteConfigUpdateRequest;
import com.eems.entity.Exhibition;
import com.eems.entity.SiteConfig;
import com.eems.mapper.SiteConfigMapper;
import com.eems.mapper.ExhibitionMapper;
import com.eems.service.SiteConfigService;
import com.eems.service.OperationLogService;
import com.eems.site.SiteContext;
import com.eems.vo.SiteConfigVO;
import com.eems.vo.PublicSiteVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

@Service
public class SiteConfigServiceImpl implements SiteConfigService {
    private static final String DEFAULT_CODE = "default";
    private final SiteConfigMapper mapper;
    private final ExhibitionMapper exhibitionMapper;
    private final OperationLogService operationLogService;
    private final FileStorageProperties fileStorageProperties;

    public SiteConfigServiceImpl(SiteConfigMapper mapper, ExhibitionMapper exhibitionMapper) {
        this(mapper, exhibitionMapper, null, new FileStorageProperties());
    }

    public SiteConfigServiceImpl(SiteConfigMapper mapper, ExhibitionMapper exhibitionMapper,
                                 OperationLogService operationLogService) {
        this(mapper, exhibitionMapper, operationLogService, new FileStorageProperties());
    }

    @Autowired
    public SiteConfigServiceImpl(SiteConfigMapper mapper, ExhibitionMapper exhibitionMapper,
                                 OperationLogService operationLogService,
                                 FileStorageProperties fileStorageProperties) {
        this.mapper = mapper;
        this.exhibitionMapper = exhibitionMapper;
        this.operationLogService = operationLogService;
        this.fileStorageProperties = fileStorageProperties;
    }

    @Override
    public SiteConfigVO getDefault() {
        return SiteConfigVO.from(findDefault());
    }

    @Override
    public PublicSiteVO getPublicDefault() {
        return PublicSiteVO.from(findDefault());
    }

    @Override
    public Long getCurrentExhibitionId() {
        return findDefault().getCurrentExhibitionId();
    }

    @Override
    @Transactional
    public SiteConfigVO updateDefault(SiteConfigUpdateRequest request) {
        return updateDefault(request, null);
    }

    @Override
    @Transactional
    public SiteConfigVO updateDefault(SiteConfigUpdateRequest request, String username) {
        validateCurrentExhibition(request.currentExhibitionId());
        String heroVideoUrl = normalizeHeroVideoUrl(request.heroVideoUrl());
        validateHeroVideoUrl(heroVideoUrl);
        SiteConfig value = findDefault();
        value.setCurrentExhibitionId(request.currentExhibitionId());
        value.setHeroVideoUrl(heroVideoUrl);
        value.setSiteTitle(request.siteTitle());
        value.setSiteSubtitle(request.siteSubtitle());
        value.setDomain(request.domain());
        value.setLogoUrl(request.logoUrl());
        value.setKeywords(request.keywords());
        value.setDescription(request.description());
        value.setIcpNumber(request.icpNumber());
        value.setTemplateCode(request.templateCode());
        value.setStatisticsCode(request.statisticsCode());
        value.setFooterInfo(request.footerInfo());
        if (mapper.updateById(value) != 1) {
            throw new BusinessException("SITE_CONFIG_UPDATE_FAILED", "默认站点配置更新失败");
        }
        if (operationLogService != null) {
            operationLogService.success("SITE_CONFIG", username, "修改站点配置", "PUT", "/api/admin/site",
                    null, "{\"configCode\":\"default\"}");
        }
        return SiteConfigVO.from(value);
    }

    private void validateCurrentExhibition(Long exhibitionId) {
        if (exhibitionId == null) {
            return;
        }
        boolean exists = exhibitionMapper.selectCount(Wrappers.<Exhibition>lambdaQuery()
                .eq(Exhibition::getId, exhibitionId)
                .eq(Exhibition::getSiteId, SiteContext.requireSiteId())) > 0;
        if (!exists) {
            throw new BusinessException("EXHIBITION_NOT_FOUND", "当前主展会不存在或已删除");
        }
    }

    private String normalizeHeroVideoUrl(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private void validateHeroVideoUrl(String value) {
        if (value == null) {
            return;
        }
        Long siteId = SiteContext.requireSiteId();
        String prefix = fileStorageProperties.getPublicUrlPrefix();
        if (!StringUtils.hasText(prefix)) {
            prefix = "/uploads";
        }
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        String expected = prefix + "/sites/" + siteId + "/hero-video/";
        if (!value.startsWith("/") || value.contains("..") || value.contains("\\")
                || value.contains("?") || value.contains("#")
                || !value.startsWith(expected) || !value.toLowerCase().endsWith(".mp4")
                || value.length() <= expected.length()) {
            throw new BusinessException("HERO_VIDEO_URL_INVALID", "首页宣传视频必须使用当前站点上传的 MP4 文件");
        }
    }

    private SiteConfig findDefault() {
        SiteConfig value = mapper.selectOne(new LambdaQueryWrapper<SiteConfig>()
                .eq(SiteConfig::getConfigCode, DEFAULT_CODE));
        if (value == null) {
            throw new BusinessException("SITE_CONFIG_NOT_FOUND", "默认站点配置不存在");
        }
        return value;
    }
}
