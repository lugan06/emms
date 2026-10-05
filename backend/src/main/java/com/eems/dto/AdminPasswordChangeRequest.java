package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "管理员修改密码请求")
public record AdminPasswordChangeRequest(
        @NotBlank(message = "oldPassword 不能为空")
        @Schema(description = "原密码", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        String oldPassword,
        @NotBlank(message = "newPassword 不能为空")
        @Size(min = 8, max = 100, message = "newPassword 长度必须为 8 到 100 位")
        @Schema(description = "新密码，至少 8 位", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
        String newPassword) {
}
