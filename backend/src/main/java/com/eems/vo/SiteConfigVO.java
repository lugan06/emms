package com.eems.vo;

import com.eems.entity.SiteConfig;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "站点配置")
public record SiteConfigVO(Long id, String configCode, Long currentExhibitionId, String siteTitle,
                           String siteSubtitle, String domain, String logoUrl, String keywords,
                           String description, String icpNumber, String templateCode,
                           String statisticsCode, String footerInfo) {
    public static SiteConfigVO from(SiteConfig value) {
        return new SiteConfigVO(value.getId(), value.getConfigCode(), value.getCurrentExhibitionId(),
                value.getSiteTitle(), value.getSiteSubtitle(), value.getDomain(), value.getLogoUrl(),
                value.getKeywords(), value.getDescription(), value.getIcpNumber(), value.getTemplateCode(),
                value.getStatisticsCode(), value.getFooterInfo());
    }
}
