package com.eems.common.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.net.URI;
import java.net.URISyntaxException;

public final class RichTextSanitizer {
    private static final Safelist BODY_SAFELIST = Safelist.relaxed()
            .addTags("img")
            .addAttributes("img", "src", "alt", "width", "height", "class")
            .removeProtocols("img", "src", "http", "https")
            .preserveRelativeLinks(true);

    private RichTextSanitizer() {
    }

    public static String sanitize(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }

        Document document = Jsoup.parseBodyFragment(body);
        for (Element image : document.select("img[src]")) {
            String source = image.attr("src").trim();
            if (isSafeImageSource(source)) {
                image.attr("src", source);
            } else {
                image.removeAttr("src");
            }
        }
        return Jsoup.clean(document.body().html(), BODY_SAFELIST);
    }

    private static boolean isSafeImageSource(String source) {
        if (source.isEmpty() || source.contains("\\") || source.chars().anyMatch(Character::isISOControl)) {
            return false;
        }
        if (source.startsWith("/") && !source.startsWith("//")) {
            return true;
        }

        try {
            URI uri = new URI(source);
            String scheme = uri.getScheme();
            return uri.getHost() != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (URISyntaxException exception) {
            return false;
        }
    }
}
