package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "栏目新增或编辑参数")
public class CategorySaveRequest {
    @NotNull
    @Min(value = 0, message = "parentId must not be negative")
    @Schema(description = "父栏目 ID，0 表示顶级栏目", example = "0")
    private Long parentId = 0L;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "栏目名称", example = "新闻资讯")
    private String name;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "栏目编码，全局唯一", example = "news")
    private String code;

    @Size(max = 150)
    @Schema(description = "公共端 URL 名称", nullable = true, example = "news")
    private String urlName;

    @NotBlank
    @Size(max = 50)
    @Schema(description = "内容模型：PAGE、ARTICLE、PRODUCT、CASE、LINK、DIRECTORY", example = "ARTICLE")
    private String modelCode = "ARTICLE";

    @Size(max = 100)
    @Schema(description = "列表页模板编码", nullable = true)
    private String listTemplate;

    @Size(max = 100)
    @Schema(description = "详情页模板编码", nullable = true)
    private String detailTemplate;

    @NotNull
    @Min(value = 0, message = "sortOrder must not be negative")
    @Schema(description = "排序值，数值越小越靠前", example = "0")
    private Integer sortOrder = 0;

    @NotNull
    @Schema(description = "是否启用：0 否，1 是", example = "1")
    private Integer isEnabled = 1;

    @NotNull
    @Schema(description = "是否显示在公共端导航：0 否，1 是", example = "1")
    private Integer showInNav = 1;

    @Size(max = 500)
    @Schema(description = "LINK 类型栏目跳转地址", nullable = true)
    private String linkUrl;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getUrlName() { return urlName; }
    public void setUrlName(String urlName) { this.urlName = urlName; }
    public String getModelCode() { return modelCode; }
    public void setModelCode(String modelCode) { this.modelCode = modelCode; }
    public String getListTemplate() { return listTemplate; }
    public void setListTemplate(String listTemplate) { this.listTemplate = listTemplate; }
    public String getDetailTemplate() { return detailTemplate; }
    public void setDetailTemplate(String detailTemplate) { this.detailTemplate = detailTemplate; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getIsEnabled() { return isEnabled; }
    public void setIsEnabled(Integer isEnabled) { this.isEnabled = isEnabled; }
    public Integer getShowInNav() { return showInNav; }
    public void setShowInNav(Integer showInNav) { this.showInNav = showInNav; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
}
