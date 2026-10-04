package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理员登录请求")
public class AdminLoginRequest {

    @NotBlank(message = "username 不能为空")
    @Schema(description = "管理员登录账号", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "password 不能为空")
    @Schema(description = "管理员登录密码", example = "your-password", requiredMode = Schema.RequiredMode.REQUIRED, format = "password")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
