package com.eems.site;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

/** Explicit allow-list shared by request validation and OpenAPI documentation. */
public final class SiteRequestPolicy {
    public static final String HEADER = "X-Site-Id";
    private static final Set<String> SITE_RESOURCES = Set.of(
            "/api/admin/site", "/api/admin/exhibitions", "/api/admin/categories",
            "/api/admin/contents", "/api/admin/files", "/api/admin/guestbooks");

    private SiteRequestPolicy() { }

    public static String path(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }

    public static boolean isAdmin(String path) {
        return path.equals("/api/admin") || path.startsWith("/api/admin/");
    }

    public static boolean isLogin(String path) {
        return path.equals("/api/admin/login");
    }

    public static boolean allowsOverride(String path) {
        return path.equals("/api/admin/me") || SITE_RESOURCES.stream()
                .anyMatch(root -> path.equals(root) || path.startsWith(root + "/"));
    }
}
