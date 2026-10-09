package com.mrsoft.arabicreference.seo.domain;

import java.time.Instant;

public record PublicLocation(String type, String path, Instant publishedAt) {

    public boolean publishable() {
        if (path == null || !path.startsWith("/") || path.contains("?") || path.contains("#") || path.contains("%")) {
            return false;
        }
        if (path.startsWith("/admin") || path.startsWith("/api") || path.startsWith("/search")) {
            return false;
        }
        return !path.contains("/attempts");
    }
}
