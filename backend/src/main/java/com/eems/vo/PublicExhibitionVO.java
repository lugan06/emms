package com.eems.vo;

import com.eems.entity.Exhibition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "公共端展会信息")
public record PublicExhibitionVO(
        @Schema(description = "展会 ID", example = "1") Long id,
        @Schema(description = "展会业务编码", example = "EEMS-2026") String exhibitionCode,
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
        @Schema(description = "发布时间", nullable = true) LocalDateTime publishedAt,
        @Schema(description = "联系人", nullable = true) String contactName,
        @Schema(description = "联系电话", nullable = true) String contactPhone,
        @Schema(description = "报名地址", nullable = true) String registrationUrl,
        @Schema(description = "是否显示在首页") Integer showOnHome,
        @Schema(description = "首页排序值") Integer homeSort,
        @Schema(description = "是否为当前展会") Boolean isCurrent) {

    public static PublicExhibitionVO from(Exhibition value, boolean current) {
        return new PublicExhibitionVO(value.getId(), value.getExhibitionCode(), value.getTitle(),
                value.getSubtitle(), value.getYear(), value.getEdition(), value.getCoverUrl(), value.getSummary(),
                value.getDescription(), value.getVenue(), value.getAddress(), value.getStartAt(), value.getEndAt(),
                value.getPublishedAt(), value.getContactName(), value.getContactPhone(), value.getRegistrationUrl(),
                value.getShowOnHome(), value.getHomeSort(), current);
    }
}
