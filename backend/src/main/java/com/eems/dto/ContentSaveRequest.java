package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "栏目内容新增或编辑参数")
public class ContentSaveRequest {
    @NotNull
    @Min(value = 1, message = "categoryId must be greater than 0")
    @Schema(description = "所属栏目 ID", example = "1")
    private Long categoryId;
    @Min(value = 1, message = "exhibitionId must be greater than 0")
    @Schema(description = "关联展会 ID", nullable = true, example = "1")
    private Long exhibitionId;
    @NotBlank
    @Size(max = 200)
    @Schema(description = "内容标题", example = "展会动态")
    private String title;
    @Size(max = 150)
    @Schema(description = "公共端 URL 标识", nullable = true, example = "exhibition-news")
    private String slug;
    @Size(max = 500)
    @Schema(description = "封面图地址", nullable = true)
    private String coverUrl;
    @Size(max = 2000)
    @Schema(description = "内容摘要", nullable = true)
    private String summary;
    @Schema(description = "富文本正文，保存前会进行基础安全过滤", nullable = true)
    private String body;
    @Size(max = 100)
    @Schema(description = "作者", nullable = true)
    private String author;
    @Size(max = 200)
    @Schema(description = "内容来源", nullable = true)
    private String source;
    @Min(value = 0, message = "sortOrder must not be negative")
    @NotNull
    @Schema(description = "排序值，数值越小越靠前", example = "0")
    private Integer sortOrder = 0;
    @NotNull
    @Schema(description = "是否置顶：0 否，1 是", example = "0")
    private Integer isTop = 0;
    @NotNull
    @Schema(description = "是否推荐：0 否，1 是", example = "0")
    private Integer isRecommend = 0;
    @Schema(description = "扩展属性 JSON", nullable = true)
    private String extraData;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getExhibitionId() { return exhibitionId; }
    public void setExhibitionId(Long exhibitionId) { this.exhibitionId = exhibitionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }
    public Integer getIsRecommend() { return isRecommend; }
    public void setIsRecommend(Integer isRecommend) { this.isRecommend = isRecommend; }
    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData; }
}
