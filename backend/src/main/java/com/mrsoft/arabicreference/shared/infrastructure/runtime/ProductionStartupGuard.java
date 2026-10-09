package com.mrsoft.arabicreference.shared.infrastructure.runtime;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
@Order(0)
public class ProductionStartupGuard implements ApplicationRunner {

    private final ProductionRequirements.Settings settings;

    public ProductionStartupGuard(
            @Value("${DB_HOST:localhost}") String dbHost,
            @Value("${DB_PASSWORD:local-dev-only}") String dbPassword,
            @Value("${REDIS_HOST:localhost}") String redisHost,
            @Value("${REDIS_PASSWORD:local-dev-only}") String redisPassword,
            @Value("${app.admin.jwt-secret}") String jwtSecret,
            @Value("${app.seo.site-url:}") String siteUrl,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.seo.indexing-enabled:false}") boolean indexingEnabled,
            @Value("${app.ai.enabled:false}") boolean aiEnabled,
            @Value("${app.ai.api-key:}") String aiApiKey,
            @Value("${app.admin.bootstrap.password:}") String bootstrapPassword) {
        this.settings = new ProductionRequirements.Settings(
                dbHost, dbPassword, redisHost, redisPassword, jwtSecret, siteUrl, frontendUrl,
                indexingEnabled, aiEnabled, aiApiKey, bootstrapPassword);
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> problems = ProductionRequirements.violations(settings);
        if (!problems.isEmpty()) {
            throw new IllegalStateException("Refusing to start with an unsafe production configuration. " + String.join(" ", problems));
        }
    }
}
