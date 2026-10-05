package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "展会分页查询参数")
public class ExhibitionPageQuery extends PageQuery {
    @Schema(description = "标题、编码或地点关键字", nullable = true) private String keyword;
    @Schema(description = "展会状态：DRAFT、PUBLISHED、OFFLINE", nullable = true) private String status;
    @Schema(description = "举办年份", nullable = true) private Integer year;
    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Integer getYear() { return year; }
    public void setYear(Integer value) { year = value; }
}
