package com.eems.vo;

import com.eems.entity.SiteConfig;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "公共端站点信息")
public record PublicSiteVO(
        @Schema(description = "站点标题", example = "EEMS 涂料博览会") String siteTitle,
        @Schema(description = "站点副标题", nullable = true) String siteSubtitle,
        @Schema(description = "站点域名", nullable = true) String domain,
        @Schema(description = "站点 Logo 地址", nullable = true) String logoUrl,
        @Schema(description = "SEO 关键词", nullable = true) String keywords,
        @Schema(description = "SEO 描述", nullable = true) String description,
        @Schema(description = "ICP备案号", nullable = true) String icpNumber,
        @Schema(description = "页脚公开信息", nullable = true) String footerInfo) {

    public static PublicSiteVO from(SiteConfig value) {
        return new PublicSiteVO(value.getSiteTitle(), value.getSiteSubtitle(), value.getDomain(),
                value.getLogoUrl(), value.getKeywords(), value.getDescription(), value.getIcpNumber(),
                value.getFooterInfo());
    }
}
