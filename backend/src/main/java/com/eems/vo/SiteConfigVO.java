package com.eems.vo;

import com.eems.entity.SiteConfig;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "站点配置")
public record SiteConfigVO(
        @Schema(description = "配置 ID") Long id,
        @Schema(description = "配置编码") String configCode,
        @Schema(description = "当前主展会 ID", nullable = true) Long currentExhibitionId,
        @Schema(description = "站点标题") String siteTitle,
        @Schema(description = "站点副标题", nullable = true) String siteSubtitle,
        @Schema(description = "站点域名", nullable = true) String domain,
        @Schema(description = "站点 Logo 地址", nullable = true) String logoUrl,
        @Schema(description = "SEO 关键词", nullable = true) String keywords,
        @Schema(description = "SEO 描述", nullable = true) String description,
        @Schema(description = "ICP备案号", nullable = true) String icpNumber,
        @Schema(description = "模板编码", nullable = true) String templateCode,
        @Schema(description = "统计代码", nullable = true) String statisticsCode,
        @Schema(description = "页脚信息", nullable = true) String footerInfo,
        @Schema(description = "首页宣传视频地址，仅允许当前站点上传的 MP4 地址", nullable = true)
        String heroVideoUrl) {
    public static SiteConfigVO from(SiteConfig value) {
        return new SiteConfigVO(value.getId(), value.getConfigCode(), value.getCurrentExhibitionId(),
                value.getSiteTitle(), value.getSiteSubtitle(), value.getDomain(), value.getLogoUrl(),
                value.getKeywords(), value.getDescription(), value.getIcpNumber(), value.getTemplateCode(),
                value.getStatisticsCode(), value.getFooterInfo(), value.getHeroVideoUrl());
    }
}
