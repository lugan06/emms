package com.eems.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "管理员基本信息")
public class AdminUserVO {

    @Schema(description = "管理员 ID", example = "1")
    private Long id;

    @Schema(description = "登录账号", example = "admin")
    private String username;

    @Schema(description = "显示名称", example = "系统管理员")
    private String nickname;

    @Schema(description = "头像地址", example = "/uploads/avatar/admin.png", nullable = true)
    private String avatarUrl;

    @Schema(description = "角色编码", example = "SUPER_ADMIN")
    private String roleCode;

    public AdminUserVO() {
    }

    public AdminUserVO(Long id, String username, String nickname, String avatarUrl, String roleCode) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.roleCode = roleCode;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getRoleCode() {
        return roleCode;
    }
}
