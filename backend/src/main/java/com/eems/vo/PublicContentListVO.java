package com.eems.vo;

import com.eems.entity.CmsCategory;
import com.eems.entity.CmsContent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "公共端栏目内容摘要")
public record PublicContentListVO(
        @Schema(description = "内容 ID") Long id,
        @Schema(description = "栏目 ID") Long categoryId,
        @Schema(description = "栏目编码") String categoryCode,
        @Schema(description = "栏目名称") String categoryName,
        @Schema(description = "关联展会 ID", nullable = true) Long exhibitionId,
        @Schema(description = "内容标题") String title,
        @Schema(description = "公共端 URL 标识", nullable = true) String slug,
        @Schema(description = "封面图地址", nullable = true) String coverUrl,
        @Schema(description = "内容摘要", nullable = true) String summary,
        @Schema(description = "作者", nullable = true) String author,
        @Schema(description = "内容来源", nullable = true) String source,
        @Schema(description = "发布时间", nullable = true) LocalDateTime publishedAt,
        @Schema(description = "是否置顶") Integer isTop,
        @Schema(description = "是否推荐") Integer isRecommend) {

    public static PublicContentListVO from(CmsContent value, CmsCategory category) {
        return new PublicContentListVO(value.getId(), value.getCategoryId(), category.getCode(), category.getName(),
                value.getExhibitionId(), value.getTitle(), value.getSlug(), value.getCoverUrl(), value.getSummary(),
                value.getAuthor(), value.getSource(), value.getPublishedAt(), value.getIsTop(), value.getIsRecommend());
    }
}
