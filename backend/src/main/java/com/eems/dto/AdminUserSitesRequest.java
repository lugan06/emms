package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;

@Schema(description = "完整替换普通管理员授权站点；旧 Token 立即失效")
public record AdminUserSitesRequest(
        @Schema(description = "真实站点 ID 列表，不能重复；空数组表示撤销全部授权")
        @NotNull @Size(max = 1000) List<@NotNull @Positive Long> siteIds) {
}

