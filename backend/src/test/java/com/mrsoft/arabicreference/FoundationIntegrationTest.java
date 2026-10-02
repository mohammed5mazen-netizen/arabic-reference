package com.mrsoft.arabicreference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mrsoft.arabicreference.linguistics.application.ArabicTextNormalizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FoundationIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ArabicTextNormalizationService normalizationService;

    @Test
    void applicationContextNormalizesWithoutReplacingOriginalText() {
        var text = normalizationService.normalize("كِتَابٌ");

        assertThat(text.originalText()).isEqualTo("كِتَابٌ");
        assertThat(text.normalizedText()).isEqualTo("كتاب");
    }

    @Test
    void flywayCreatesTheFoundationMarker() {
        Integer applied = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success = true and version = '1'",
                Integer.class);
        Integer tables = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'foundation_marker'",
                Integer.class);

        assertThat(applied).isEqualTo(1);
        assertThat(tables).isEqualTo(1);
    }

    @Test
    void healthIsPublicAndIncludesDatabaseAndRedis() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.redis.status").value("UP"));
    }

    @Test
    void publicFoundationDoesNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/public/foundation"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Trace-Id"))
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.data.access").value("public-anonymous"))
                .andExpect(jsonPath("$.data.stage").value("S0"))
                .andExpect(jsonPath("$.data.message").value("المرجع العربي متاح للقراءة العامة دون تسجيل دخول."));
    }

    @Test
    void siteRootIsNotAnAuthenticationChallenge() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isNotFound())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void unknownPublicReadIsNotUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/public/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("The requested resource was not found."));
    }

    @Test
    void adminFoundationIsClosed() throws Exception {
        mockMvc.perform(get("/api/v1/admin/foundation"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"POST", "PUT", "PATCH", "DELETE"})
    void publicApiRejectsContentMutations(String method) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(method), "/api/v1/public/foundation"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void hiddenActuatorEndpointsAreNotPublic() throws Exception {
        int status = mockMvc.perform(get("/actuator/env")).andReturn().getResponse().getStatus();

        assertThat(status).isIn(401, 403);
    }
}
