package com.eems.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "普通管理员资料及授权，不包含密码或认证版本")
public record ManagedAdminUserVO(
        @Schema(description = "管理员 ID") Long id,
        @Schema(description = "用户名") String username,
        @Schema(description = "昵称") String nickname,
        @Schema(description = "ENABLED 或 DISABLED") String status,
        @Schema(description = "授权站点 ID 列表") List<Long> siteIds,
        @Schema(description = "授权站点名称、编码及状态") List<SiteOptionVO> sites,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {
}

