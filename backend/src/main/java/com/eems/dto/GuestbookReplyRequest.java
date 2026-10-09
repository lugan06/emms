package com.eems.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "在线留言回复参数")
public class GuestbookReplyRequest {
    @NotBlank(message = "replyContent 不能为空")
    @Size(max = 2000, message = "replyContent 长度不能超过 2000")
    @Schema(description = "回复内容", example = "您好，展位申请已开放。", requiredMode = Schema.RequiredMode.REQUIRED)
    private String replyContent;

    public String getReplyContent() { return replyContent; }
    public void setReplyContent(String replyContent) { this.replyContent = replyContent; }
}
