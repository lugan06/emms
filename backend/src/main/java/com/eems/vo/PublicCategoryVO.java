package com.eems.vo;

import com.eems.entity.CmsCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "公共端栏目导航节点")
public record PublicCategoryVO(
        @Schema(description = "栏目 ID") Long id,
        @Schema(description = "父栏目 ID，0 表示顶级栏目") Long parentId,
        @Schema(description = "栏目名称") String name,
        @Schema(description = "栏目编码") String code,
        @Schema(description = "公共端 URL 名称", nullable = true) String urlName,
        @Schema(description = "栏目模型") String modelCode,
        @Schema(description = "跳转地址，LINK 类型栏目使用", nullable = true) String linkUrl,
        @Schema(description = "子栏目") List<PublicCategoryVO> children) {

    public static PublicCategoryVO from(CmsCategory value, List<PublicCategoryVO> children) {
        return new PublicCategoryVO(value.getId(), value.getParentId(), value.getName(), value.getCode(),
                value.getUrlName(), value.getModelCode(), value.getLinkUrl(), children);
    }
}
