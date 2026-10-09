package com.eems.dto;

import com.eems.common.model.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Schema(description = "操作日志分页查询参数")
public class OperationLogPageQuery extends PageQuery {
    @Schema(description = "操作人账号，支持精确匹配", nullable = true, example = "admin")
    private String username;

    @Schema(description = "业务模块", nullable = true, example = "CMS_CONTENT")
    private String module;

    @Schema(description = "操作类型，支持模糊匹配", nullable = true, example = "发布")
    private String operation;

    @Pattern(regexp = "SUCCESS|FAILURE", message = "result 必须是 SUCCESS 或 FAILURE")
    @Schema(description = "操作结果：SUCCESS 或 FAILURE", nullable = true, example = "SUCCESS")
    private String result;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "开始时间，格式 yyyy-MM-dd HH:mm:ss", nullable = true, example = "2026-10-01 00:00:00")
    private LocalDateTime startAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "结束时间，格式 yyyy-MM-dd HH:mm:ss", nullable = true, example = "2026-10-31 23:59:59")
    private LocalDateTime endAt;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public LocalDateTime getStartAt() { return startAt; }
    public void setStartAt(LocalDateTime startAt) { this.startAt = startAt; }
    public LocalDateTime getEndAt() { return endAt; }
    public void setEndAt(LocalDateTime endAt) { this.endAt = endAt; }
}
