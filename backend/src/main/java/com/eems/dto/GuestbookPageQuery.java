package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "在线留言分页查询参数")
public class GuestbookPageQuery extends PageQuery {
    @Pattern(regexp = "UNREAD|READ|REPLIED|CLOSED", message = "status 必须是 UNREAD、READ、REPLIED 或 CLOSED")
    @Schema(description = "留言状态：UNREAD、READ、REPLIED、CLOSED", nullable = true, example = "UNREAD")
    private String status;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
