package com.eems.site;

public final class SiteContextTestSupport {
    private SiteContextTestSupport() {
    }

    public static void selectDefaultSite() {
        selectSite(1L);
    }

    public static void selectSite(long siteId) {
        SiteContext.setSiteId(siteId);
    }

    public static void clear() {
        SiteContext.clear();
    }
}
