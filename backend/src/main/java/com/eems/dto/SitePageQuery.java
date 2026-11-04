package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "全局站点分页查询")
public class SitePageQuery extends PageQuery {
    @Schema(description = "按名称、编码或绑定域名模糊搜索")
    @Size(max = 253)
    private String keyword;
    @Schema(description = "状态：ENABLED 或 DISABLED，省略则查询全部")
    @Pattern(regexp = "ENABLED|DISABLED")
    private String status;
    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
}

