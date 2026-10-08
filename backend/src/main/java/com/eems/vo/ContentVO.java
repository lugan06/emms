package com.eems.vo;

import com.eems.entity.CmsContent;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "栏目内容管理信息")
public record ContentVO(
        @Schema(description = "内容 ID") Long id,
        @Schema(description = "所属栏目 ID") Long categoryId,
        @Schema(description = "关联展会 ID", nullable = true) Long exhibitionId,
        @Schema(description = "内容标题") String title,
        @Schema(description = "公共端 URL 标识", nullable = true) String slug,
        @Schema(description = "封面图地址", nullable = true) String coverUrl,
        @Schema(description = "内容摘要", nullable = true) String summary,
        @Schema(description = "富文本正文", nullable = true) String body,
        @Schema(description = "作者", nullable = true) String author,
        @Schema(description = "内容来源", nullable = true) String source,
        @Schema(description = "内容状态：DRAFT、PUBLISHED、OFFLINE") String status,
        @Schema(description = "发布时间", nullable = true) LocalDateTime publishedAt,
        @Schema(description = "排序值") Integer sortOrder,
        @Schema(description = "是否置顶：0 否，1 是") Integer isTop,
        @Schema(description = "是否推荐：0 否，1 是") Integer isRecommend,
        @Schema(description = "浏览次数") Integer viewCount,
        @Schema(description = "扩展属性 JSON", nullable = true) String extraData,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {

    public static ContentVO from(CmsContent value) {
        return new ContentVO(value.getId(), value.getCategoryId(), value.getExhibitionId(), value.getTitle(),
                value.getSlug(), value.getCoverUrl(), value.getSummary(), value.getBody(), value.getAuthor(),
                value.getSource(), value.getStatus(), value.getPublishedAt(), value.getSortOrder(),
                value.getIsTop(), value.getIsRecommend(), value.getViewCount(), value.getExtraData(),
                value.getCreatedAt(), value.getUpdatedAt());
    }
}
