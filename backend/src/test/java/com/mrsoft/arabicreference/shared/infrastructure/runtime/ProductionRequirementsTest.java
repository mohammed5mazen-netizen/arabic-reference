package com.mrsoft.arabicreference.shared.infrastructure.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProductionRequirementsTest {

    private static final String SECURE_JWT = "a-production-signing-key-with-32b";

    @Test
    void localPlaceholdersAreRejected() {
        ProductionRequirements.Settings local = new ProductionRequirements.Settings(
                "localhost", "local-dev-only", "127.0.0.1", "local-dev-only",
                "local-dev-only-admin-jwt-secret-key-32b", "http://localhost:3000", "http://localhost:3000",
                true, true, "", "123456");
        assertThat(ProductionRequirements.violations(local)).isNotEmpty();
    }

    @Test
    void aPublicHttpsConfigurationIsAccepted() {
        ProductionRequirements.Settings ready = new ProductionRequirements.Settings(
                "db.internal", "db-password-not-local", "redis.internal", "redis-password-not-local",
                "a-production-signing-key-with-32b", "https://reference.example", "https://reference.example",
                false, false, "", "");
        assertThat(ProductionRequirements.violations(ready)).isEmpty();
    }

    @Test
    void missingShortAndPlaceholderJwtSecretsAreRejected() {
        assertThat(problems(settings("redis.internal", "redis-password-not-local", "", "https://reference.example", "https://reference.example")))
                .anyMatch(problem -> problem.contains("ADMIN_JWT_SECRET is not set"));
        assertThat(problems(settings("redis.internal", "redis-password-not-local", "short-secret", "https://reference.example", "https://reference.example")))
                .anyMatch(problem -> problem.contains("shorter than 32"));
        assertThat(problems(settings("redis.internal", "redis-password-not-local", "local-dev-only-admin-jwt-secret-key-32b", "https://reference.example", "https://reference.example")))
                .anyMatch(problem -> problem.contains("local development placeholder"));
    }

    @Test
    void localRedisIsRejectedAndARemoteRedisHostIsAccepted() {
        assertThat(problems(settings("localhost", "redis-password-not-local", SECURE_JWT, "https://reference.example", "https://reference.example")))
                .anyMatch(problem -> problem.contains("REDIS_HOST"));
        assertThat(problems(settings("redis.internal", "local-dev-only", SECURE_JWT, "https://reference.example", "https://reference.example")))
                .anyMatch(problem -> problem.contains("REDIS_PASSWORD"));
        assertThat(problems(settings("red-cache.internal", "redis-password-not-local", SECURE_JWT, "https://reference.example", "https://reference.example")))
                .isEmpty();
    }

    @Test
    void siteAndFrontendOriginsMustBePublicHttps() {
        assertThat(problems(settings("redis.internal", "redis-password-not-local", SECURE_JWT, "http://reference.example", "https://reference.example")))
                .anyMatch(problem -> problem.contains("SITE_URL"));
        assertThat(problems(settings("redis.internal", "redis-password-not-local", SECURE_JWT, "https://reference.example/admin", "https://reference.example")))
                .anyMatch(problem -> problem.contains("SITE_URL"));
        assertThat(problems(settings("redis.internal", "redis-password-not-local", SECURE_JWT, "https://reference.example", "http://localhost:3000")))
                .anyMatch(problem -> problem.contains("FRONTEND_URL"));
        assertThat(problems(settings("redis.internal", "redis-password-not-local", SECURE_JWT, "https://arabic-reference.vercel.app", "https://arabic-reference.vercel.app")))
                .isEmpty();
    }

    @Test
    void indexingAndAiStayExplicit() {
        ProductionRequirements.Settings indexing = new ProductionRequirements.Settings(
                "db.internal", "db-password-not-local", "redis.internal", "redis-password-not-local",
                "a-production-signing-key-with-32b", "https://reference.example/", "https://reference.example",
                false, true, "", "");
        assertThat(ProductionRequirements.violations(indexing)).anyMatch(problem -> problem.contains("AI_API_KEY"));
    }

    private static ProductionRequirements.Settings settings(
            String redisHost, String redisPassword, String jwtSecret, String siteUrl, String frontendUrl) {
        return new ProductionRequirements.Settings(
                "db.internal", "db-password-not-local", redisHost, redisPassword, jwtSecret, siteUrl, frontendUrl,
                false, false, "", "");
    }

    private static List<String> problems(ProductionRequirements.Settings settings) {
        return ProductionRequirements.violations(settings);
    }
}
