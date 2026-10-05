package com.mrsoft.arabicreference;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class IntegrationContainers {

    public static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withCommand("postgres", "-c", "max_connections=300");

    @SuppressWarnings("resource")
    public static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--requirepass", "local-dev-only");

    static {
        POSTGRES.start();
        REDIS.start();
    }

    private IntegrationContainers() {
    }

    public static void register(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> Integer.toString(REDIS.getMappedPort(6379)));
        registry.add("spring.data.redis.password", () -> "local-dev-only");
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "4");
        registry.add("app.frontend-url", () -> "http://localhost:3000");
        registry.add("app.admin.jwt-secret", () -> "test-only-admin-jwt-secret-key-32b!");
    }
}
