package com.eems.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理员登录响应数据")
public class AdminLoginResponse {

    @Schema(description = "JWT 访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    @Schema(description = "令牌有效期，单位为秒", example = "7200")
    private long expiresIn;

    @Schema(description = "当前管理员信息")
    private AdminUserVO user;

    public AdminLoginResponse(String token, String tokenType, long expiresIn, AdminUserVO user) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public AdminUserVO getUser() {
        return user;
    }
}
