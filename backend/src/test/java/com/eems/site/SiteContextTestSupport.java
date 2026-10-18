package com.eems.site;

public final class SiteContextTestSupport {
    private SiteContextTestSupport() {
    }

    public static void selectDefaultSite() {
        SiteContext.setSiteId(1L);
    }

    public static void clear() {
        SiteContext.clear();
    }
}
