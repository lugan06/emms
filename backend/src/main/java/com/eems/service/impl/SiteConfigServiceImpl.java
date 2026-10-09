package com.eems.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.eems.common.exception.BusinessException;
import com.eems.dto.SiteConfigUpdateRequest;
import com.eems.entity.Exhibition;
import com.eems.entity.SiteConfig;
import com.eems.mapper.SiteConfigMapper;
import com.eems.mapper.ExhibitionMapper;
import com.eems.service.SiteConfigService;
import com.eems.service.OperationLogService;
import com.eems.vo.SiteConfigVO;
import com.eems.vo.PublicSiteVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class SiteConfigServiceImpl implements SiteConfigService {
    private static final String DEFAULT_CODE = "default";
    private final SiteConfigMapper mapper;
    private final ExhibitionMapper exhibitionMapper;
    private final OperationLogService operationLogService;

    public SiteConfigServiceImpl(SiteConfigMapper mapper, ExhibitionMapper exhibitionMapper) {
        this(mapper, exhibitionMapper, null);
    }

    @Autowired
    public SiteConfigServiceImpl(SiteConfigMapper mapper, ExhibitionMapper exhibitionMapper,
                                 OperationLogService operationLogService) {
        this.mapper = mapper;
        this.exhibitionMapper = exhibitionMapper;
        this.operationLogService = operationLogService;
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
        SiteConfig value = findDefault();
        value.setCurrentExhibitionId(request.currentExhibitionId());
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
                .eq(Exhibition::getId, exhibitionId)) > 0;
        if (!exists) {
            throw new BusinessException("EXHIBITION_NOT_FOUND", "当前主展会不存在或已删除");
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
