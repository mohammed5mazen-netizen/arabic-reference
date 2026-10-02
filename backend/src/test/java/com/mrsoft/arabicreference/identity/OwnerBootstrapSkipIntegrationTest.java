package com.mrsoft.arabicreference.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.IntegrationContainers;
import java.sql.Connection;
import java.sql.DriverManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class OwnerBootstrapSkipIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) throws Exception {
        IntegrationContainers.register(registry);
        String adminUrl = IntegrationContainers.POSTGRES.getJdbcUrl();
        String skipUrl = adminUrl.substring(0, adminUrl.lastIndexOf('/') + 1) + "bootstrap_skip";
        try (Connection connection = DriverManager.getConnection(
                adminUrl,
                IntegrationContainers.POSTGRES.getUsername(),
                IntegrationContainers.POSTGRES.getPassword())) {
            connection.createStatement().execute("CREATE DATABASE bootstrap_skip");
        } catch (Exception exception) {
            if (exception.getMessage() == null || !exception.getMessage().contains("already exists")) {
                throw exception;
            }
        }
        registry.add("spring.datasource.url", () -> skipUrl);
        registry.add("app.admin.bootstrap.username", () -> "");
        registry.add("app.admin.bootstrap.password", () -> "");
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void missingBootstrapVariablesDoNotCreateAnOwner() {
        Integer owners = jdbc.queryForObject("select count(*) from admin_user", Integer.class);
        Integer roles = jdbc.queryForObject("select count(*) from admin_role", Integer.class);
        assertThat(owners).isZero();
        assertThat(roles).isEqualTo(6);
    }
}
