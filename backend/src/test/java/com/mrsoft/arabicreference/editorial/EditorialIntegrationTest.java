package com.mrsoft.arabicreference.editorial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mrsoft.arabicreference.IntegrationContainers;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class EditorialIntegrationTest {

    static final UUID REVIEWER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000004");
    static final String OWNER_PASSWORD = "Owner-Pass-123!";

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.rate-limit.login", () -> "1000");
        registry.add("app.admin.rate-limit.refresh", () -> "1000");
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> OWNER_PASSWORD);
        registry.add("app.editorial.bulk-limit", () -> "100");
        registry.add("spring.jpa.properties.hibernate.generate_statistics", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void operationsCenterUsesRealCountsAndKeepsInternalNotesPrivate() throws Exception {
        mockMvc.perform(get("/api/v1/admin/editorial/dashboard")).andExpect(status().isUnauthorized());
        String editor = member("s11e", UUID.fromString("a0000000-0000-4000-8000-000000000003"));
        String reviewer = member("s11r", REVIEWER_ROLE);
        String publisher = member("s11p", UUID.fromString("a0000000-0000-4000-8000-000000000005"));
        mockMvc.perform(get("/api/v1/admin/editorial/quality/findings").header("Authorization", bearer(editor)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/editorial/dashboard").header("Authorization", bearer(reviewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.draft").isNumber())
                .andExpect(jsonPath("$.data.qualityIssues").isNumber());

        String source = publishSource(editor, reviewer, publisher, "مقاييس اللغة");
        String citation = body(mockMvc.perform(post("/api/v1/admin/sources/" + id(source) + "/citations")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pageFrom\":1,\"pageTo\":2,\"entryLabel\":\"كتب\"}"))
                .andExpect(status().isOk()).andReturn());
        String entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lemma\":\"مرجع\",\"vocalizedForm\":null,\"rootId\":null,\"partOfSpeech\":\"NOUN\",\"gender\":\"MASCULINE\"}"))
                .andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/editorial/quality/scans")
                        .header("Authorization", bearer(publisher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scope\":\"RECORD\",\"contentType\":\"DICTIONARY_ENTRY\",\"contentId\":\"" + id(entry) + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/editorial/quality/findings").param("type", "DICTIONARY_ENTRY").param("code", "ENTRY_WITHOUT_SENSE")
                        .header("Authorization", bearer(reviewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].severity").value("BLOCKER"));

        entry = addSense(editor, entry);
        entry = linkSense(editor, entry, citation);
        entry = act(editor, "/api/v1/admin/dictionary/entries/" + id(entry) + "/submit", entry);
        entry = act(reviewer, "/api/v1/admin/dictionary/entries/" + id(entry) + "/verify", entry);
        mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/publish")
                        .header("Authorization", bearer(publisher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(entry))))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/v1/admin/editorial/quality/scans")
                        .header("Authorization", bearer(publisher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scope\":\"RECORD\",\"contentType\":\"DICTIONARY_ENTRY\",\"contentId\":\"" + id(entry) + "\"}"))
                .andExpect(status().isOk());
        Integer open = jdbc.queryForObject("""
                select count(*) from quality_finding
                where content_id = ? and code = 'ENTRY_WITHOUT_SENSE' and status = 'OPEN'
                """, Integer.class, UUID.fromString(id(entry)));
        assertThat(open).isZero();
        entry = act(publisher, "/api/v1/admin/dictionary/entries/" + id(entry) + "/publish", entry);
        mockMvc.perform(get("/api/v1/public/dictionary/by-slug/" + slug(entry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lemmaOriginal").value("مرجع"));

        long contentVersion = version(entry);
        String comment = body(mockMvc.perform(post("/api/v1/admin/editorial/records/DICTIONARY_ENTRY/" + id(entry) + "/comments")
                        .header("Authorization", bearer(reviewer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kind\":\"CORRECTION\",\"body\":\"اضبط المثال\",\"expectedVersion\":" + contentVersion + "}"))
                .andExpect(status().isOk()).andReturn());
        mockMvc.perform(get("/api/v1/public/dictionary/by-slug/" + slug(entry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.senses[0].definition").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("اضبط المثال"))));
        mockMvc.perform(post("/api/v1/admin/editorial/comments/" + id(comment) + "/resolve")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":1}"))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/v1/admin/editorial/comments/" + id(comment) + "/resolve")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":0}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/editorial/records/DICTIONARY_ENTRY/" + id(entry) + "/timeline")
                        .header("Authorization", bearer(reviewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].kind").exists());
        mockMvc.perform(get("/api/v1/admin/editorial/queue").param("type", "DICTIONARY_ENTRY").param("q", "مرجع")
                        .header("Authorization", bearer(editor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber())
                .andExpect(jsonPath("$.data.items[0].href").value("/admin/dictionary/" + id(entry)));

        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        mockMvc.perform(get("/api/v1/admin/editorial/dashboard").header("Authorization", bearer(reviewer))).andExpect(status().isOk());
        assertThat(statistics.getPrepareStatementCount()).isBetween(0L, 25L);
    }

    @Test
    void assignmentDiffBulkAndSourcesStayInsideTheirRules() throws Exception {
        String editor = member("s11e2", UUID.fromString("a0000000-0000-4000-8000-000000000003"));
        String reviewer = member("s11r2", REVIEWER_ROLE);
        String otherReviewer = member("s11r3", REVIEWER_ROLE);
        String publisher = member("s11p2", UUID.fromString("a0000000-0000-4000-8000-000000000005"));
        String source = publishSource(editor, reviewer, publisher, "العين");
        String duplicate = body(mockMvc.perform(post("/api/v1/admin/sources")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceType\":\"BOOK\",\"title\":\"العين\",\"author\":\"الخليل\",\"edition\":\"1\",\"licenseType\":\"CC_BY\",\"publicDomain\":false,\"attributionText\":\"نسب\"}"))
                .andExpect(status().isOk()).andReturn());
        mockMvc.perform(get("/api/v1/admin/sources/duplicates").header("Authorization", bearer(editor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
        mockMvc.perform(delete("/api/v1/admin/sources/" + id(source))
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(source))))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/v1/admin/sources/" + id(duplicate))
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(duplicate))))
                .andExpect(status().isOk());

        String entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lemma\":\"عين\",\"vocalizedForm\":null,\"rootId\":null,\"partOfSpeech\":\"NOUN\",\"gender\":\"FEMININE\"}"))
                .andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/editorial/records/DICTIONARY_ENTRY/" + id(entry) + "/assign")
                        .header("Authorization", bearer(reviewer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"REVIEWER\",\"assigneeId\":\"" + userId(editor) + "\",\"version\":0}"))
                .andExpect(status().isForbidden());
        String assignee = userId(otherReviewer);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        race(() -> count(statusOf(post("/api/v1/admin/editorial/records/DICTIONARY_ENTRY/" + id(entry) + "/assign")
                .header("Authorization", bearer(reviewer))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"REVIEWER\",\"assigneeId\":\"" + assignee + "\",\"version\":0}")), success, conflict));
        assertThat(success.get()).isEqualTo(1);
        assertThat(conflict.get()).isEqualTo(1);

        String updated = body(mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/admin/dictionary/entries/" + id(entry))
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + version(entry) + ",\"draft\":{\"lemma\":\"عينٌ\",\"vocalizedForm\":null,\"rootId\":null,\"partOfSpeech\":\"NOUN\",\"gender\":\"FEMININE\"}}"))
                .andExpect(status().isOk()).andReturn());
        mockMvc.perform(get("/api/v1/admin/editorial/records/DICTIONARY_ENTRY/" + id(entry) + "/diff")
                        .header("Authorization", bearer(reviewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromRevision").value(0));
        String analysis = body(mockMvc.perform(post("/api/v1/admin/morphology/analyses")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lexicalEntryId\":\"" + id(entry) + "\",\"notes\":\"كِتاب\"}"))
                .andExpect(status().isOk()).andReturn());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/api/v1/admin/morphology/analyses/" + id(analysis))
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + version(analysis) + ",\"draft\":{\"lexicalEntryId\":\"" + id(entry) + "\",\"notes\":\"كِتابٌ\"}}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/editorial/records/MORPHOLOGY_ANALYSIS/" + id(analysis) + "/diff")
                        .param("fromRevision", "1").param("toRevision", "2")
                        .header("Authorization", bearer(reviewer)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fromRevision").value(1))
                .andExpect(jsonPath("$.data.toRevision").value(2));
        mockMvc.perform(post("/api/v1/admin/editorial/records/DICTIONARY_ENTRY/" + id(updated) + "/comments")
                        .header("Authorization", bearer(reviewer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kind\":\"GENERAL\",\"body\":\"قديم\",\"expectedVersion\":" + version(entry) + "}"))
                .andExpect(status().isConflict());

        StringBuilder items = new StringBuilder("[");
        for (int index = 0; index < 101; index++) {
            if (index > 0) {
                items.append(',');
            }
            items.append("{\"type\":\"DICTIONARY_ENTRY\",\"id\":\"").append(id(entry)).append("\",\"version\":0}");
        }
        items.append(']');
        mockMvc.perform(post("/api/v1/admin/editorial/bulk")
                        .header("Authorization", bearer(reviewer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"ASSIGN_REVIEWER\",\"assigneeId\":\"" + assignee + "\",\"items\":" + items + "}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/editorial/bulk")
                        .header("Authorization", bearer(publisher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"PUBLISH\",\"items\":[{\"type\":\"DICTIONARY_ENTRY\",\"id\":\"" + id(entry) + "\",\"version\":0}]}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/editorial/bulk")
                        .header("Authorization", bearer(reviewer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"ASSIGN_REVIEWER\",\"assigneeId\":\"" + userId(editor) + "\",\"items\":[{\"type\":\"DICTIONARY_ENTRY\",\"id\":\"" + id(entry) + "\",\"version\":1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].result").value("FAILED"));
        mockMvc.perform(get("/api/v1/admin/editorial/records/DICTIONARY_ENTRY/" + id(entry) + "/readiness")
                        .header("Authorization", bearer(publisher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state").value("BLOCKED"));
        mockMvc.perform(get("/api/v1/admin/sources/" + id(source) + "/usage").header("Authorization", bearer(publisher)))
                .andExpect(status().isOk());
    }

    private String publishSource(String editor, String reviewer, String publisher, String title) throws Exception {
        String source = body(mockMvc.perform(post("/api/v1/admin/sources")
                        .header("Authorization", bearer(editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceType\":\"BOOK\",\"title\":\"" + title + "\",\"author\":\"الخليل\",\"edition\":\"1\",\"licenseType\":\"CC_BY\",\"publicDomain\":false,\"attributionText\":\"نسب\"}"))
                .andExpect(status().isOk()).andReturn());
        source = act(editor, "/api/v1/admin/sources/" + id(source) + "/submit", source);
        source = act(reviewer, "/api/v1/admin/sources/" + id(source) + "/verify", source);
        return act(publisher, "/api/v1/admin/sources/" + id(source) + "/publish", source);
    }

    private String addSense(String token, String entry) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/senses")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + version(entry) + ",\"sense\":{\"definition\":\"أصل يُرجع إليه\",\"shortDefinition\":\"أصل\",\"usageLabel\":\"CLASSICAL\",\"domainLabel\":\"LANGUAGE\",\"displayOrder\":1}}"))
                .andExpect(status().isOk()).andReturn());
    }

    private String linkSense(String token, String entry, String citation) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/senses/" + senseId(entry) + "/citations")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":" + version(entry) + ",\"citationId\":\"" + id(citation) + "\"}"))
                .andExpect(status().isOk()).andReturn());
    }

    private String member(String username, UUID role) throws Exception {
        String owner = token("owner", OWNER_PASSWORD);
        MvcResult created = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", bearer(owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"email\":\"" + username + "@arabic-reference.test\",\"displayName\":\"" + username + "\",\"roleIds\":[\"" + role + "\"]}"))
                .andExpect(status().isOk()).andReturn();
        String temporary = JsonPath.read(created.getResponse().getContentAsString(), "$.data.temporaryPassword");
        String current = token(username, temporary);
        MvcResult changed = mockMvc.perform(post("/api/v1/admin/auth/change-password")
                        .header("Authorization", bearer(current))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + temporary + "\",\"newPassword\":\"Staff-Pass-123!\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(changed.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private String token(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private String userId(String accessToken) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/admin/auth/session").header("Authorization", bearer(accessToken)))
                .andExpect(status().isOk()).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
    }

    private String act(String token, String path, String resource) throws Exception {
        return body(mockMvc.perform(post(path)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(resource))))
                .andExpect(status().isOk()).andReturn());
    }

    private void race(Runnable action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?>[] futures = new Future<?>[2];
            for (int index = 0; index < 2; index++) {
                futures[index] = executor.submit(() -> {
                    try {
                        start.await();
                        action.run();
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(30, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static void count(int statusCode, AtomicInteger success, AtomicInteger conflict) {
        if (statusCode == 200) {
            success.incrementAndGet();
        } else if (statusCode == 409) {
            conflict.incrementAndGet();
        } else {
            throw new AssertionError("Unexpected status " + statusCode);
        }
    }

    private int statusOf(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        try {
            return mockMvc.perform(request).andReturn().getResponse().getStatus();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static String versionBody(long version) {
        return "{\"version\":" + version + "}";
    }

    private static String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString();
    }

    private static String id(String payload) {
        return JsonPath.read(payload, "$.data.id");
    }

    private static String slug(String payload) {
        return JsonPath.read(payload, "$.data.slug");
    }

    private static String senseId(String payload) {
        return JsonPath.read(payload, "$.data.senses[0].id");
    }

    private static long version(String payload) {
        Number value = JsonPath.read(payload, "$.data.version");
        return value.longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
