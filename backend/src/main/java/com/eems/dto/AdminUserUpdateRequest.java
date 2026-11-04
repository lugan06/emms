package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "更新普通管理员基本资料，不接受角色、状态或密码修改")
public record AdminUserUpdateRequest(
        @Schema(description = "昵称，1–100 字符") @NotBlank @Size(max = 100) String nickname) {
}

