package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "超管重置普通管理员密码")
public record AdminUserResetPasswordRequest(
        @Schema(description = "新密码，8–72 字符且 UTF-8 不超过 72 字节", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank @Size(min = 8, max = 72) String password) {
}

