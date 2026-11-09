package com.eems.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "首页展会概览子栏目及其完整公开内容")
public record PublicHomeSectionVO(
        @Schema(description = "栏目 ID") Long categoryId,
        @Schema(description = "栏目编码") String categoryCode,
        @Schema(description = "栏目名称") String categoryName,
        @Schema(description = "当前展会及未关联展会的已发布内容，含安全过滤后的完整正文") List<PublicContentVO> items) {
}
