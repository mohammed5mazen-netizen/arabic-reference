package com.mrsoft.arabicreference.shared.infrastructure.runtime;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Production refuses the local development placeholders. The check lists problems and does not print secret values.
 */
public final class ProductionRequirements {

    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "::1", "0.0.0.0");
    private static final String LOCAL_PASSWORD = "local-dev-only";
    private static final String LOCAL_JWT = "local-dev-only-admin-jwt-secret-key-32b";

    private ProductionRequirements() {
    }

    public record Settings(
            String dbHost,
            String dbPassword,
            String redisHost,
            String redisPassword,
            String jwtSecret,
            String siteUrl,
            String frontendUrl,
            boolean indexingEnabled,
            boolean aiEnabled,
            String aiApiKey,
            String bootstrapPassword) {
    }

    public static List<String> violations(Settings settings) {
        List<String> problems = new ArrayList<>();
        if (localHost(settings.dbHost())) {
            problems.add("DB_HOST must not be a local address in production.");
        }
        if (blank(settings.dbPassword()) || LOCAL_PASSWORD.equals(settings.dbPassword())) {
            problems.add("DB_PASSWORD is missing or still the local development placeholder.");
        }
        if (localHost(settings.redisHost())) {
            problems.add("REDIS_HOST must not be a local address in production.");
        }
        if (blank(settings.redisPassword()) || LOCAL_PASSWORD.equals(settings.redisPassword())) {
            problems.add("REDIS_PASSWORD is missing or still the local development placeholder.");
        }
        if (blank(settings.jwtSecret()) || settings.jwtSecret().length() < 32 || LOCAL_JWT.equals(settings.jwtSecret())) {
            problems.add("ADMIN_JWT_SECRET is missing, shorter than 32 characters, or still the local development placeholder.");
        }
        if (!publicHttps(settings.siteUrl())) {
            problems.add("SITE_URL must be an absolute public https origin.");
        }
        if (!publicHttps(settings.frontendUrl())) {
            problems.add("FRONTEND_URL must be an absolute public https origin.");
        }
        if (settings.indexingEnabled() && !publicHttps(settings.siteUrl())) {
            problems.add("SEO indexing requires a public https SITE_URL.");
        }
        if (settings.aiEnabled() && blank(settings.aiApiKey())) {
            problems.add("AI_ENABLED requires AI_API_KEY.");
        }
        if (!blank(settings.bootstrapPassword()) && weakBootstrap(settings.bootstrapPassword())) {
            problems.add("BOOTSTRAP_OWNER_PASSWORD is not acceptable in production.");
        }
        return problems;
    }

    private static boolean weakBootstrap(String password) {
        String value = password.toLowerCase(Locale.ROOT);
        return value.equals("admin")
                || value.equals("123456")
                || value.equals("password")
                || value.contains(LOCAL_PASSWORD)
                || password.length() < 12;
    }

    private static boolean localHost(String host) {
        if (blank(host)) {
            return true;
        }
        String value = host.trim().toLowerCase(Locale.ROOT);
        return LOCAL_HOSTS.contains(value) || value.endsWith(".local");
    }

    static boolean publicHttps(String raw) {
        if (blank(raw)) {
            return false;
        }
        URI uri;
        try {
            uri = URI.create(raw.trim());
        } catch (IllegalArgumentException exception) {
            return false;
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            return false;
        }
        if (uri.getRawQuery() != null || uri.getRawFragment() != null || uri.getUserInfo() != null) {
            return false;
        }
        String path = uri.getPath();
        if (path != null && !path.isBlank() && !"/".equals(path)) {
            return false;
        }
        return !localHost(uri.getHost());
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
