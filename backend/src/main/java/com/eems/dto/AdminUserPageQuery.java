package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "普通管理员分页查询")
public class AdminUserPageQuery extends PageQuery {
    @Schema(description = "用户名或昵称模糊搜索") @Size(max = 100)
    private String keyword;
    @Schema(description = "ENABLED 或 DISABLED") @Pattern(regexp = "ENABLED|DISABLED")
    private String status;
    @Schema(description = "按授权站点 ID 筛选") @Positive
    private Long siteId;
    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long value) { siteId = value; }
}

