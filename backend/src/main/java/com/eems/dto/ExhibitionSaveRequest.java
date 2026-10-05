package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "展会新增或编辑参数")
public class ExhibitionSaveRequest {
    @NotBlank @Size(max = 100)
    @Schema(description = "展会业务编码", example = "EEMS-2026")
    private String exhibitionCode;
    @NotBlank @Size(max = 200)
    @Schema(description = "展会标题", example = "2026中国国际涂料博览会")
    private String title;
    @Size(max = 200) @Schema(description = "展会副标题", nullable = true) private String subtitle;
    @NotNull @Schema(description = "举办年份", example = "2026") private Integer year;
    @Size(max = 50) @Schema(description = "届次", nullable = true) private String edition;
    @Size(max = 500) @Schema(description = "封面图地址", nullable = true) private String coverUrl;
    @Schema(description = "展会简介", nullable = true) private String summary;
    @Schema(description = "展会详细描述", nullable = true) private String description;
    @Size(max = 200) @Schema(description = "展馆或举办地点", nullable = true) private String venue;
    @Size(max = 500) @Schema(description = "详细地址", nullable = true) private String address;
    @NotNull @Schema(description = "开始时间", example = "2026-06-10 09:00:00") private LocalDateTime startAt;
    @NotNull @Schema(description = "结束时间", example = "2026-06-12 17:00:00") private LocalDateTime endAt;
    @Size(max = 500) @Schema(description = "报名地址", nullable = true) private String registrationUrl;
    @Size(max = 100) @Schema(description = "联系人", nullable = true) private String contactName;
    @Size(max = 30) @Schema(description = "联系电话", nullable = true) private String contactPhone;
    @Schema(description = "是否显示在首页", example = "0") private Integer showOnHome = 0;
    @Schema(description = "首页排序值", example = "0") private Integer homeSort = 0;
    @Schema(description = "扩展属性 JSON", nullable = true) private String extraData;

    public String getExhibitionCode() { return exhibitionCode; }
    public void setExhibitionCode(String value) { exhibitionCode = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String value) { subtitle = value; }
    public Integer getYear() { return year; }
    public void setYear(Integer value) { year = value; }
    public String getEdition() { return edition; }
    public void setEdition(String value) { edition = value; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String value) { coverUrl = value; }
    public String getSummary() { return summary; }
    public void setSummary(String value) { summary = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public String getVenue() { return venue; }
    public void setVenue(String value) { venue = value; }
    public String getAddress() { return address; }
    public void setAddress(String value) { address = value; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime value) { startAt = value; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime value) { endAt = value; }
    public String getRegistrationUrl() { return registrationUrl; }
    public void setRegistrationUrl(String value) { registrationUrl = value; }
    public String getContactName() { return contactName; }
    public void setContactName(String value) { contactName = value; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String value) { contactPhone = value; }
    public Integer getShowOnHome() { return showOnHome; }
    public void setShowOnHome(Integer value) { showOnHome = value; }
    public Integer getHomeSort() { return homeSort; }
    public void setHomeSort(Integer value) { homeSort = value; }
    public String getExtraData() { return extraData; }
    public void setExtraData(String value) { extraData = value; }
}
