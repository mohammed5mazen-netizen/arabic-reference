package com.mrsoft.arabicreference.shared.infrastructure.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProductionRequirementsTest {

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
    void indexingAndAiStayExplicit() {
        ProductionRequirements.Settings indexing = new ProductionRequirements.Settings(
                "db.internal", "db-password-not-local", "redis.internal", "redis-password-not-local",
                "a-production-signing-key-with-32b", "https://reference.example/", "https://reference.example",
                false, true, "", "");
        assertThat(ProductionRequirements.violations(indexing)).anyMatch(problem -> problem.contains("AI_API_KEY"));
    }
}
