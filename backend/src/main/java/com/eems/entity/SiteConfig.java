package com.eems.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("site_config")
public class SiteConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String configCode;
    private Long currentExhibitionId;
    private String siteTitle;
    private String siteSubtitle;
    private String domain;
    private String logoUrl;
    private String keywords;
    private String description;
    private String icpNumber;
    private String templateCode;
    private String statisticsCode;
    private String footerInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getConfigCode() { return configCode; }
    public void setConfigCode(String configCode) { this.configCode = configCode; }
    public Long getCurrentExhibitionId() { return currentExhibitionId; }
    public void setCurrentExhibitionId(Long currentExhibitionId) { this.currentExhibitionId = currentExhibitionId; }
    public String getSiteTitle() { return siteTitle; }
    public void setSiteTitle(String siteTitle) { this.siteTitle = siteTitle; }
    public String getSiteSubtitle() { return siteSubtitle; }
    public void setSiteSubtitle(String siteSubtitle) { this.siteSubtitle = siteSubtitle; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcpNumber() { return icpNumber; }
    public void setIcpNumber(String icpNumber) { this.icpNumber = icpNumber; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getStatisticsCode() { return statisticsCode; }
    public void setStatisticsCode(String statisticsCode) { this.statisticsCode = statisticsCode; }
    public String getFooterInfo() { return footerInfo; }
    public void setFooterInfo(String footerInfo) { this.footerInfo = footerInfo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
