package com.mrsoft.arabicreference.shared.infrastructure.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.identity.application.AdminSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class AdminJwtSecretBindingTest {

    private static final String SECURE_JWT = "a-production-signing-key-with-32b!!";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(Probe.class);

    @Test
    void productionAcceptsAdminJwtSecretFromTheEnvironmentName() {
        runner.withSystemProperties(
                        "spring.profiles.active=prod",
                        "ADMIN_JWT_SECRET=" + SECURE_JWT,
                        "DB_HOST=db.internal",
                        "DB_NAME=arabic_reference")
                .run(context -> assertThat(context).getBean(AdminSecurityProperties.class)
                        .extracting(AdminSecurityProperties::getJwtSecret)
                        .isEqualTo(SECURE_JWT));
    }

    @Test
    void productionDoesNotFallBackToTheLocalPlaceholder() {
        runner.withSystemProperties(
                        "spring.profiles.active=prod",
                        "DB_HOST=db.internal",
                        "DB_NAME=arabic_reference")
                .run(context -> assertThat(context.getEnvironment().getProperty("app.admin.jwt-secret"))
                        .as("prod must not keep the local JWT default")
                        .isEqualTo("unset-admin-jwt-secret"));
    }

    @Test
    void productionDatasourceRequiresSslAndTheRenderPort() {
        runner.withSystemProperties(
                        "spring.profiles.active=prod",
                        "ADMIN_JWT_SECRET=" + SECURE_JWT,
                        "DB_HOST=ep-example.region.aws.neon.tech",
                        "DB_PORT=5432",
                        "DB_NAME=arabic_reference",
                        "PORT=10000")
                .run(context -> {
                    assertThat(context.getEnvironment().getProperty("spring.datasource.url"))
                            .isEqualTo("jdbc:postgresql://ep-example.region.aws.neon.tech:5432/arabic_reference?sslmode=require");
                    assertThat(context.getEnvironment().getProperty("server.port")).isEqualTo("10000");
                });
    }

    @Configuration
    @EnableConfigurationProperties(AdminSecurityProperties.class)
    static class Probe {
    }
}
