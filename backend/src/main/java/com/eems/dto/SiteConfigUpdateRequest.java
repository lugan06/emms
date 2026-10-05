package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "站点配置更新请求")
public record SiteConfigUpdateRequest(
        @NotBlank @Size(max = 200) @Schema(description = "站点标题", requiredMode = Schema.RequiredMode.REQUIRED) String siteTitle,
        @Size(max = 200) @Schema(description = "站点副标题") String siteSubtitle,
        @Size(max = 255) @Schema(description = "站点域名") String domain,
        @Size(max = 500) @Schema(description = "Logo 地址") String logoUrl,
        @Size(max = 500) @Schema(description = "SEO 关键词") String keywords,
        @Schema(description = "SEO 描述") String description,
        @Size(max = 100) @Schema(description = "ICP备案号") String icpNumber,
        @Size(max = 100) @Schema(description = "模板编码") String templateCode,
        @Schema(description = "统计代码") String statisticsCode,
        @Schema(description = "页脚信息") String footerInfo,
        @Schema(description = "当前主展会 ID") Long currentExhibitionId) {
}
