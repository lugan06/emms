package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "站点启停请求")
public record SiteStatusRequest(
        @Schema(description = "目标状态：ENABLED 启用、DISABLED 停用")
        @NotNull @Pattern(regexp = "ENABLED|DISABLED") String status) {
}

