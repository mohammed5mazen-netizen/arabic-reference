package com.mrsoft.arabicreference.seo.domain;

import java.net.URI;
import java.util.Locale;
import java.util.Optional;

public record SiteOrigin(String value) {

    public static Optional<SiteOrigin> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String trimmed = raw.trim();
        URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            return Optional.empty();
        }
        if (uri.getRawQuery() != null || uri.getRawFragment() != null || uri.getUserInfo() != null) {
            return Optional.empty();
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            return Optional.empty();
        }
        String path = uri.getPath();
        if (path != null && !path.isBlank() && !path.equals("/")) {
            return Optional.empty();
        }
        String port = uri.getPort() < 0 ? "" : ":" + uri.getPort();
        return Optional.of(new SiteOrigin(scheme + "://" + uri.getHost().toLowerCase(Locale.ROOT) + port));
    }

    public boolean indexable() {
        URI uri = URI.create(value);
        if (!"https".equals(uri.getScheme())) {
            return false;
        }
        String host = uri.getHost().toLowerCase(Locale.ROOT);
        return !host.equals("localhost") && !host.equals("127.0.0.1") && !host.equals("::1") && !host.endsWith(".local");
    }
}
