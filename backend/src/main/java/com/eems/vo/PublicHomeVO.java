package com.eems.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "公共首页聚合数据")
public record PublicHomeVO(
        @Schema(description = "公共站点信息") PublicSiteVO site,
        @Schema(description = "公共公司信息") PublicCompanyVO company,
        @Schema(description = "当前展会", nullable = true) PublicExhibitionVO currentExhibition,
        @Schema(description = "展会简介") List<PublicContentListVO> exhibitionIntro,
        @Schema(description = "同期活动") List<PublicContentListVO> samePeriodActivities,
        @Schema(description = "参展范围") List<PublicContentListVO> exhibitionScope,
        @Schema(description = "展会动态") List<PublicContentListVO> exhibitionNews,
        @Schema(description = "行业新闻") List<PublicContentListVO> industryNews,
        @Schema(description = "展商新闻") List<PublicContentListVO> exhibitorNews) {
}
