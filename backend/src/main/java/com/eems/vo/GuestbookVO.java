package com.eems.vo;

import com.eems.entity.Guestbook;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "在线留言管理信息，联系方式和 IP 已脱敏")
public record GuestbookVO(
        @Schema(description = "留言 ID", example = "1") Long id,
        @Schema(description = "留言人姓名", example = "张先生") String name,
        @Schema(description = "脱敏后的联系电话", example = "138****8000", nullable = true) String phone,
        @Schema(description = "脱敏后的联系邮箱", example = "c***@example.com", nullable = true) String email,
        @Schema(description = "公司名称", nullable = true) String companyName,
        @Schema(description = "留言内容") String message,
        @Schema(description = "留言状态：UNREAD、READ、REPLIED、CLOSED") String status,
        @Schema(description = "回复内容", nullable = true) String replyContent,
        @Schema(description = "回复管理员 ID", nullable = true) Long repliedBy,
        @Schema(description = "回复时间", nullable = true) LocalDateTime repliedAt,
        @Schema(description = "脱敏后的提交 IP", example = "192.0.2.*", nullable = true) String ipAddress,
        @Schema(description = "提交时间") LocalDateTime createdAt,
        @Schema(description = "更新时间") LocalDateTime updatedAt) {

    public static GuestbookVO from(Guestbook value) {
        return new GuestbookVO(value.getId(), value.getName(), maskPhone(value.getPhone()),
                maskEmail(value.getEmail()), value.getCompanyName(), value.getMessage(), value.getStatus(),
                value.getReplyContent(), value.getRepliedBy(), value.getRepliedAt(), maskIp(value.getIpAddress()),
                value.getCreatedAt(), value.getUpdatedAt());
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        if (phone.length() <= 4) {
            return "****";
        }
        if (phone.length() <= 7) {
            return phone.substring(0, 2) + "***" + phone.substring(phone.length() - 2);
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private static String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return email;
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    private static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return ip;
        }
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".*";
        }
        return "***";
    }
}
