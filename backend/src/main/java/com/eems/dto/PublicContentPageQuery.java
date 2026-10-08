package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "公共栏目内容分页参数")
public class PublicContentPageQuery extends PageQuery {
    @Schema(description = "内容标题关键字", nullable = true)
    private String keyword;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
