package com.eems.vo;

import com.eems.dto.SiteDomainRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "站点及其完整域名绑定")
public record ManagedSiteVO(
        @Schema(description = "站点 ID") Long id,
        @Schema(description = "站点名称") String name,
        @Schema(description = "不可修改的唯一编码") String code,
        @Schema(description = "ENABLED 或 DISABLED") String status,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt,
        @Schema(description = "主域名与别名列表") List<SiteDomainRequest> domains) {
}

