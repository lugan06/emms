package com.eems.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "公共首页聚合数据")
public record PublicHomeVO(
        @Schema(description = "公共站点信息") PublicSiteVO site,
        @Schema(description = "公共公司信息") PublicCompanyVO company,
        @Schema(description = "当前展会", nullable = true) PublicExhibitionVO currentExhibition,
        @Schema(description = "展会简介，含安全过滤后的富文本正文") List<PublicContentVO> exhibitionIntro,
        @Schema(description = "同期活动，含安全过滤后的富文本正文") List<PublicContentVO> samePeriodActivities,
        @Schema(description = "参展范围，含安全过滤后的富文本正文") List<PublicContentVO> exhibitionScope,
        @Schema(description = "展会动态，含安全过滤后的富文本正文") List<PublicContentVO> exhibitionNews,
        @Schema(description = "行业新闻，含安全过滤后的富文本正文") List<PublicContentVO> industryNews,
        @Schema(description = "展商新闻，含安全过滤后的富文本正文") List<PublicContentVO> exhibitorNews) {
}
