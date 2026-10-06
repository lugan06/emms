package com.eems.vo;

import com.eems.entity.CmsCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "栏目树节点")
public record CategoryVO(
        @Schema(description = "栏目 ID") Long id,
        @Schema(description = "父栏目 ID，0 表示顶级栏目") Long parentId,
        @Schema(description = "栏目名称") String name,
        @Schema(description = "栏目编码") String code,
        @Schema(description = "公共端 URL 名称", nullable = true) String urlName,
        @Schema(description = "内容模型：PAGE、ARTICLE、PRODUCT、CASE、LINK、DIRECTORY") String modelCode,
        @Schema(description = "列表页模板编码", nullable = true) String listTemplate,
        @Schema(description = "详情页模板编码", nullable = true) String detailTemplate,
        @Schema(description = "排序值") Integer sortOrder,
        @Schema(description = "是否启用：0 否，1 是") Integer isEnabled,
        @Schema(description = "是否显示在公共端导航：0 否，1 是") Integer showInNav,
        @Schema(description = "LINK 类型栏目跳转地址", nullable = true) String linkUrl,
        @Schema(description = "创建时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt,
        @Schema(description = "子栏目") List<CategoryVO> children) {

    public static CategoryVO from(CmsCategory value, List<CategoryVO> children) {
        return new CategoryVO(value.getId(), value.getParentId(), value.getName(), value.getCode(),
                value.getUrlName(), value.getModelCode(), value.getListTemplate(), value.getDetailTemplate(),
                value.getSortOrder(), value.getIsEnabled(), value.getShowInNav(), value.getLinkUrl(),
                value.getCreatedAt(), value.getUpdatedAt(), children);
    }
}
