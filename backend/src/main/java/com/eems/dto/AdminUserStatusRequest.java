package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "普通管理员启停请求")
public record AdminUserStatusRequest(
        @Schema(description = "ENABLED 启用或 DISABLED 禁用；状态变更使旧 Token 失效")
        @NotNull @Pattern(regexp = "ENABLED|DISABLED") String status) {
}

