package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "栏目内容分页查询参数")
public class ContentPageQuery extends PageQuery {
    @Schema(description = "栏目 ID", nullable = true, example = "1")
    private Long categoryId;
    @Schema(description = "展会 ID", nullable = true, example = "1")
    private Long exhibitionId;
    @Schema(description = "内容标题关键字", nullable = true)
    private String keyword;
    @Schema(description = "内容状态：DRAFT、PUBLISHED、OFFLINE", nullable = true)
    private String status;
    @Schema(description = "是否置顶：0 否，1 是", nullable = true)
    private Integer isTop;
    @Schema(description = "是否推荐：0 否，1 是", nullable = true)
    private Integer isRecommend;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Long getExhibitionId() { return exhibitionId; }
    public void setExhibitionId(Long exhibitionId) { this.exhibitionId = exhibitionId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getIsTop() { return isTop; }
    public void setIsTop(Integer isTop) { this.isTop = isTop; }
    public Integer getIsRecommend() { return isRecommend; }
    public void setIsRecommend(Integer isRecommend) { this.isRecommend = isRecommend; }
}
