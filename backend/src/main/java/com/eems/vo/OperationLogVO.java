package com.eems.vo;

import com.eems.entity.OperationLog;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "操作日志信息，敏感参数和 IP 已脱敏")
public record OperationLogVO(
        @Schema(description = "日志 ID", example = "1") Long id,
        @Schema(description = "操作人 ID", nullable = true, example = "1") Long adminUserId,
        @Schema(description = "操作人账号", nullable = true, example = "admin") String username,
        @Schema(description = "业务模块", example = "CMS_CONTENT") String module,
        @Schema(description = "操作类型", example = "发布内容") String operation,
        @Schema(description = "请求方法", example = "POST") String requestMethod,
        @Schema(description = "请求地址", example = "/api/admin/contents/1/publish") String requestUrl,
        @Schema(description = "脱敏后的请求 IP", nullable = true, example = "192.0.2.*") String requestIp,
        @Schema(description = "已脱敏的请求参数 JSON", nullable = true) String requestParams,
        @Schema(description = "操作结果：SUCCESS 或 FAILURE", example = "SUCCESS") String result,
        @Schema(description = "失败原因，成功时为空", nullable = true) String errorMessage,
        @Schema(description = "操作时间") LocalDateTime createdAt) {

    public static OperationLogVO from(OperationLog value) {
        return new OperationLogVO(value.getId(), value.getAdminUserId(), value.getUsername(), value.getModule(),
                value.getOperation(), value.getRequestMethod(), value.getRequestUrl(), maskIp(value.getRequestIp()),
                redact(value.getRequestParams()), value.getResult(), redact(value.getErrorMessage()), value.getCreatedAt());
    }

    private static String redact(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String masked = value
                .replaceAll("(?i)(\\\"?(?:password|oldPassword|newPassword|phone|mobile|email|message|replyContent|ipAddress)\\\"?\\s*:\\s*\\\")([^\\\"]*)(\\\")", "$1***$3")
                .replaceAll("(?i)(password|oldPassword|newPassword|phone|mobile|email|message|replyContent|ipAddress)\\s*=\\s*[^,;\\s]+", "$1=***");
        return masked.replaceAll("(?<![0-9])1[3-9][0-9]{9}(?![0-9])", "***PHONE***")
                .replaceAll("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", "***@***");
    }

    private static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return ip;
        }
        int lastDot = ip.lastIndexOf('.');
        return lastDot > 0 ? ip.substring(0, lastDot) + ".*" : "***";
    }
}
