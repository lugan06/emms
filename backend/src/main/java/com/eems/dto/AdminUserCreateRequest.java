package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.util.List;

@Schema(description = "创建普通管理员，角色固定为 EDITOR")
public record AdminUserCreateRequest(
        @Schema(description = "唯一用户名，1–50 位字母、数字、下划线、点、@ 或连字符")
        @NotBlank @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9_.@-]{0,49}") String username,
        @Schema(description = "初始密码，8–72 字符且 UTF-8 不超过 72 字节", format = "password", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank @Size(min = 8, max = 72) String password,
        @Schema(description = "昵称，1–100 字符") @NotBlank @Size(max = 100) String nickname,
        @Schema(description = "授权站点 ID，允许空数组表示无授权，最多 1000 个")
        @NotNull @Size(max = 1000) List<@NotNull @Positive Long> siteIds) {
}

