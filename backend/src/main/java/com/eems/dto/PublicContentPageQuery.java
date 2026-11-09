package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "公共栏目内容分页参数")
public class PublicContentPageQuery extends PageQuery {
    @Schema(description = "内容标题关键字", nullable = true)
    private String keyword;

    @Schema(description = "是否包含启用的子孙栏目，禁用栏目及其子树不返回", defaultValue = "false")
    private boolean includeChildren;

    @Pattern(regexp = "DEFAULT|LATEST", message = "sort must be DEFAULT or LATEST")
    @Schema(description = "DEFAULT 为置顶、推荐、排序号优先；LATEST 为发布时间降序，同时间按 ID 降序", allowableValues = {"DEFAULT", "LATEST"}, defaultValue = "DEFAULT")
    private String sort = "DEFAULT";

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public boolean isIncludeChildren() { return includeChildren; }
    public void setIncludeChildren(boolean value) { includeChildren = value; }
    public String getSort() { return sort; }
    public void setSort(String value) { sort = value; }
}
