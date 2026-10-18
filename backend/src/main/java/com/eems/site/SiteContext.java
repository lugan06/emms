package com.eems.site;

public final class SiteContext {
    private static final ThreadLocal<Long> CURRENT_SITE = new ThreadLocal<>();

    private SiteContext() {
    }

    public static Long requireSiteId() {
        Long siteId = CURRENT_SITE.get();
        if (siteId == null) {
            throw new IllegalStateException("Site context is missing");
        }
        return siteId;
    }

    public static Long currentSiteIdOrNull() {
        return CURRENT_SITE.get();
    }

    static void setSiteId(Long siteId) {
        if (siteId == null) {
            throw new IllegalArgumentException("siteId must not be null");
        }
        CURRENT_SITE.set(siteId);
    }

    static void clear() {
        CURRENT_SITE.remove();
    }
}
