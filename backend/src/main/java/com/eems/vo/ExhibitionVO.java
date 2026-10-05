package com.eems.vo;

import com.eems.entity.Exhibition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "展会信息")
public record ExhibitionVO(
        @Schema(description = "展会 ID") Long id,
        @Schema(description = "展会业务编码") String exhibitionCode,
        @Schema(description = "展会标题") String title,
        @Schema(description = "展会副标题", nullable = true) String subtitle,
        @Schema(description = "举办年份") Integer year,
        @Schema(description = "届次", nullable = true) String edition,
        @Schema(description = "封面图地址", nullable = true) String coverUrl,
        @Schema(description = "展会简介", nullable = true) String summary,
        @Schema(description = "展会详细描述", nullable = true) String description,
        @Schema(description = "展馆或举办地点", nullable = true) String venue,
        @Schema(description = "详细地址", nullable = true) String address,
        @Schema(description = "开始时间") LocalDateTime startAt,
        @Schema(description = "结束时间") LocalDateTime endAt,
        @Schema(description = "状态：DRAFT、PUBLISHED、OFFLINE") String status,
        @Schema(description = "是否显示在首页") Integer showOnHome,
        @Schema(description = "首页排序值") Integer homeSort,
        @Schema(description = "发布时间", nullable = true) LocalDateTime publishedAt,
        @Schema(description = "联系人", nullable = true) String contactName,
        @Schema(description = "联系电话", nullable = true) String contactPhone,
        @Schema(description = "报名地址", nullable = true) String registrationUrl,
        @Schema(description = "扩展属性 JSON", nullable = true) String extraData,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {
    public static ExhibitionVO from(Exhibition v) {
        return new ExhibitionVO(v.getId(), v.getExhibitionCode(), v.getTitle(), v.getSubtitle(), v.getYear(),
                v.getEdition(), v.getCoverUrl(), v.getSummary(), v.getDescription(), v.getVenue(), v.getAddress(),
                v.getStartAt(), v.getEndAt(), v.getStatus(), v.getShowOnHome(), v.getHomeSort(), v.getPublishedAt(),
                v.getContactName(), v.getContactPhone(), v.getRegistrationUrl(), v.getExtraData(), v.getCreatedAt(), v.getUpdatedAt());
    }
}
