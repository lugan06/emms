package com.eems.entity;

import java.time.LocalDateTime;

/**
 * A global authorization binding, identified by both administrator and site ID.
 * This is deliberately not a tenant-filled entity or a single-ID BaseMapper model.
 */
public class AdminUserSite {
    private Long adminUserId;
    private Long siteId;
    private LocalDateTime createdAt;

    public Long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(Long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public Long getSiteId() {
        return siteId;
    }

    public void setSiteId(Long siteId) {
        this.siteId = siteId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
