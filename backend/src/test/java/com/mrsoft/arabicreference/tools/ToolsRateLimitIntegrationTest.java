package com.mrsoft.arabicreference.tools;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mrsoft.arabicreference.IntegrationContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ToolsRateLimitIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> "Owner-Pass-123!");
        registry.add("app.tools.rate-limit-per-minute", () -> "2");
        registry.add("app.tools.graph-nodes", () -> "2");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicToolsReturn429WhenTheMinuteCeilingIsExceeded() throws Exception {
        String client = "10.9.9.9";
        mockMvc.perform(get("/api/v1/public/tools/patterns").header("X-Forwarded-For", client)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/tools/patterns").header("X-Forwarded-For", client)).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/tools/patterns").header("X-Forwarded-For", client))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("حد استخدام الأدوات")));
    }

    @Test
    void graphNodeCapIsConfigured() throws Exception {
        mockMvc.perform(get("/api/v1/public/tools/explore").param("q", "كتاب").header("X-Forwarded-For", "10.9.9.8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.nodes.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(2)));
    }
}
