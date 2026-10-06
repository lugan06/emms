package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "公共展会分页查询参数")
public class PublicExhibitionPageQuery extends PageQuery {
    @Schema(description = "展会标题、编码或地点关键字", nullable = true)
    private String keyword;

    @Schema(description = "举办年份", nullable = true, example = "2026")
    private Integer year;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
