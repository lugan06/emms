package com.eems.entity;

/** Read model for global domain bindings, including the owning site. */
public class SiteDomain {
    private Long siteId;
    private String domain;
    private Boolean primary;
    public Long getSiteId() { return siteId; }
    public void setSiteId(Long value) { siteId = value; }
    public String getDomain() { return domain; }
    public void setDomain(String value) { domain = value; }
    public Boolean getPrimary() { return primary; }
    public void setPrimary(Boolean value) { primary = value; }
}

