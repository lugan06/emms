package com.eems.site;

import java.util.Locale;
import java.util.regex.Pattern;

public final class HostParser {
    private static final Pattern LABEL = Pattern.compile("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?");

    private HostParser() {
    }

    public static String parse(String hostHeader) {
        if (hostHeader == null || hostHeader.isBlank()) {
            throw invalidHost();
        }
        String value = hostHeader.trim().toLowerCase(Locale.ROOT);
        String hostname;
        String port = null;
        if (value.startsWith("[")) {
            int close = value.indexOf(']');
            if (close < 0 || close == 1) {
                throw invalidHost();
            }
            hostname = value.substring(0, close + 1);
            if (!hostname.substring(1, close).matches("[0-9a-f:.]+")) {
                throw invalidHost();
            }
            if (close + 1 < value.length()) {
                if (value.charAt(close + 1) != ':') {
                    throw invalidHost();
                }
                port = value.substring(close + 2);
            }
        } else {
            int colon = value.indexOf(':');
            hostname = colon < 0 ? value : value.substring(0, colon);
            if (colon >= 0) {
                port = value.substring(colon + 1);
            }
            if (hostname.endsWith(".")) {
                hostname = hostname.substring(0, hostname.length() - 1);
            }
            if (hostname.isEmpty() || hostname.length() > 253) {
                throw invalidHost();
            }
            for (String label : hostname.split("\\.", -1)) {
                if (!LABEL.matcher(label).matches()) {
                    throw invalidHost();
                }
            }
        }
        if (port != null) {
            if (!port.matches("[0-9]{1,5}")) {
                throw invalidHost();
            }
            int number = Integer.parseInt(port);
            if (number < 1 || number > 65535) {
                throw invalidHost();
            }
        }
        return hostname;
    }

    private static SiteResolutionException invalidHost() {
        return new SiteResolutionException("INVALID_HOST", "无效的请求域名", 400);
    }
}
