package com.eems.vo;

import com.eems.entity.Site;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "可授权站点选项，包含停用站点")
public record SiteOptionVO(
        @Schema(description = "站点 ID") Long id,
        @Schema(description = "站点名称") String name,
        @Schema(description = "站点编码") String code,
        @Schema(description = "ENABLED 或 DISABLED") String status) {
    public static SiteOptionVO from(Site site) {
        return new SiteOptionVO(site.getId(), site.getName(), site.getCode(), site.getStatus());
    }
}

