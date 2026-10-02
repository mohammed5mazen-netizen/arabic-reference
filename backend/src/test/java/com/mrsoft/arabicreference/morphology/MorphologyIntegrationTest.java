package com.mrsoft.arabicreference.morphology;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class MorphologyIntegrationTest {

    private static final UUID EDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000003");
    private static final UUID REVIEWER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000004");
    private static final UUID PUBLISHER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000005");
    private static final UUID AUDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    private static final String FA3ALA = "b0000000-0000-4000-8000-000000000001";
    private static final AtomicInteger SEQUENCE = new AtomicInteger(40);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.rate-limit.login", () -> "1000");
        registry.add("app.admin.rate-limit.refresh", () -> "1000");
        registry.add("app.morphology.rate-limit-per-minute", () -> "10000");
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> "Owner-Pass-123!");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void publicAnalysisStaysAnonymousAndAdminMorphologyDoesNot() throws Exception {
        Integer version4 = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = true and version = '4'", Integer.class);
        assertThat(version4).isEqualTo(1);
        mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", "كتاب"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ruleSetVersion").value("s3-sound-2026-10-03"))
                .andExpect(jsonPath("$.data.analyses").isArray());
        mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", " "))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", "book"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", "ا".repeat(81)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/morphology/patterns")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/morphology/coverage")).andExpect(status().isUnauthorized());
    }

    @Test
    void dictionaryFirstAnalysisKeepsAmbiguityAndRefusesUnsupportedConjugation() throws Exception {
        Staff staff = staff();
        mockMvc.perform(get("/api/v1/admin/morphology/patterns").header("Authorization", bearer(staff.auditor)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/morphology/patterns")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"EXTRA","patternOriginal":"فَعَلَ","category":"VERB","radicalCount":3}
                                """))
                .andExpect(status().isForbidden());

        String root = publishRoot(staff, "كتب");
        String citation = citation(staff);
        String noun = publishEntry(staff, "كتاب", "كِتَاب", id(root), "NOUN", "ما يُكتب فيه", citation);
        noun = addForm(staff.editor, noun, "PLURAL", "كُتُب");
        noun = publishEntryAgain(staff, noun);
        String verb = publishEntry(staff, "كتب", "كَتَبَ", id(root), "VERB", "خط بالقلم", citation);

        mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", "والكتاب"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analyses[?(@.lexicalEntryId == '" + id(noun) + "' && @.segmentation.prefixes[0] == 'ال' && @.segmentation.clitics[0] == 'و')]").isNotEmpty());
        mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", "كاتب"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analyses[?(@.provenance == 'RULE_DERIVED' && @.patternOriginal == 'فَاعِل' && @.root == 'كتب')]").isNotEmpty());
        mockMvc.perform(get("/api/v1/public/dictionary/entries/" + id(noun) + "/morphology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.recordedPlurals[0]").value("كُتُب"))
                .andExpect(jsonPath("$.data.readings").isEmpty());

        String draft = createAnalysis(staff.editor, id(verb), FA3ALA, "SOUND", "DAMMA");
        mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", "كتب"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analyses[?(@.provenance == 'MANUAL_VERIFIED')]").isEmpty());
        mockMvc.perform(get("/api/v1/public/morphology/conjugate").param("entryId", id(verb)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverage").value("UNSUPPORTED"))
                .andExpect(jsonPath("$.data.forms").isEmpty());

        mockMvc.perform(post("/api/v1/admin/morphology/analyses/" + id(draft) + "/verify")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(draft))))
                .andExpect(status().isForbidden());
        String submitted = act(staff.editor, "/api/v1/admin/morphology/analyses/" + id(draft) + "/submit", draft);
        String verified = act(staff.reviewer, "/api/v1/admin/morphology/analyses/" + id(draft) + "/verify", submitted);
        mockMvc.perform(post("/api/v1/admin/morphology/analyses/" + id(draft) + "/publish")
                        .header("Authorization", bearer(staff.reviewer))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(verified))))
                .andExpect(status().isForbidden());
        String published = act(staff.publisher, "/api/v1/admin/morphology/analyses/" + id(draft) + "/publish", verified);
        MvcResult cited = mockMvc.perform(post("/api/v1/admin/morphology/analyses/" + id(draft) + "/citations")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"citationId":"%s"}
                                """.formatted(version(published), id(citation))))
                .andReturn();
        assertThat(cited.getResponse().getStatus()).as(cited.getResponse().getContentAsString()).isEqualTo(200);

        mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", "كَتَبَ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analyses[0].provenance").value("MANUAL_VERIFIED"))
                .andExpect(jsonPath("$.data.resultClass").value("AMBIGUOUS"))
                .andExpect(jsonPath("$.data.analyses[?(@.provenance == 'EXACT_DICTIONARY')]").isNotEmpty());
        mockMvc.perform(get("/api/v1/public/morphology/conjugate").param("entryId", id(verb)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverage").value("SUPPORTED"))
                .andExpect(jsonPath("$.data.forms[?(@.surface == 'يَكْتُبُ' && @.origin == 'RULE_GENERATED')]").isNotEmpty());
        mockMvc.perform(get("/api/v1/public/dictionary/entries/" + id(verb) + "/morphology"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.readings[0].patternOriginal").value("فَعَلَ"))
                .andExpect(jsonPath("$.data.readings[0].citationIds").isNotEmpty());

        String jalasaRoot = publishRoot(staff, "جلس");
        String partial = publishAnalysis(staff, id(publishEntry(staff, "جلس", null, id(jalasaRoot), "VERB", "قعد", citation)), FA3ALA, "SOUND", null);
        mockMvc.perform(get("/api/v1/public/morphology/conjugate").param("entryId", partial))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverage").value("PARTIALLY_SUPPORTED"))
                .andExpect(jsonPath("$.data.forms[?(@.features.aspect == 'IMPERFECT')]").isEmpty());

        String hollowEntry = publishEntry(staff, "قال", null, id(publishRoot(staff, "قول")), "VERB", "تلفظ", citation);
        publishAnalysis(staff, id(hollowEntry), FA3ALA, "HOLLOW", "DAMMA");
        mockMvc.perform(get("/api/v1/public/morphology/conjugate").param("entryId", id(hollowEntry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.coverage").value("UNSUPPORTED"))
                .andExpect(jsonPath("$.data.forms").isEmpty());

        String quad = publishEntry(staff, "دحرج", null, id(publishRoot(staff, "دحرج")), "VERB", "أدار", citation);
        mockMvc.perform(post("/api/v1/admin/morphology/analyses")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(analysisBody(id(quad), FA3ALA, "SOUND", null)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/morphology/analyses")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(analysisBody(id(verb), UUID.randomUUID().toString(), "SOUND", null)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/admin/morphology/analyses")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lexicalEntryId":"%s","patternId":"%s","features":{"person":"FOURTH"}}
                                """.formatted(id(verb), FA3ALA)))
                .andExpect(status().isBadRequest());

        for (int index = 0; index < 12; index++) {
            mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", "كتاب"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.analyses.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(8)));
        }
        assertThat(jdbc.queryForObject(
                "select count(*) from admin_audit_event where event_type = 'MORPHOLOGY_ANALYSIS_PUBLISHED'", Integer.class)).isGreaterThan(0);
    }

    @Test
    void patternEditsUseOptimisticLockingAndRulesStayDeclarative() throws Exception {
        Staff staff = staff();
        String created = body(mockMvc.perform(post("/api/v1/admin/morphology/patterns")
                        .header("Authorization", bearer(staff.owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"TEST_PATTERN_%d","patternOriginal":"فَعَلَ","category":"OTHER","radicalCount":3,"description":"اختبار"}
                                """.formatted(SEQUENCE.incrementAndGet())))
                .andExpect(status().isOk())
                .andReturn());
        long current = version(created);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        race(2, () -> count(statusOf(patch("/api/v1/admin/morphology/patterns/" + id(created))
                .header("Authorization", bearer(staff.owner))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"version":%d,"patternOriginal":"فَعَلَ","category":"OTHER","radicalCount":3,"description":"تعديل"}
                        """.formatted(current))), success, conflict));
        assertThat(success.get()).isEqualTo(1);
        assertThat(conflict.get()).isEqualTo(1);

        String rules = body(mockMvc.perform(get("/api/v1/admin/morphology/rules").header("Authorization", bearer(staff.owner)))
                .andExpect(status().isOk())
                .andReturn());
        int ruleVersion = ruleVersion(rules, "R-FA3IL");
        try {
            mockMvc.perform(post("/api/v1/admin/morphology/rules/R-FA3IL")
                            .header("Authorization", bearer(staff.owner))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"enabled":false,"version":%d}
                                    """.formatted(ruleVersion)))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/v1/public/morphology/analyze").param("word", "كاتب"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.analyses[?(@.provenance == 'RULE_DERIVED' && @.patternCode == 'FA3IL')]").isEmpty());
        } finally {
            String after = body(mockMvc.perform(get("/api/v1/admin/morphology/rules").header("Authorization", bearer(staff.owner)))
                    .andExpect(status().isOk())
                    .andReturn());
            int nextVersion = ruleVersion(after, "R-FA3IL");
            mockMvc.perform(post("/api/v1/admin/morphology/rules/R-FA3IL")
                            .header("Authorization", bearer(staff.owner))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"enabled":true,"version":%d}
                                    """.formatted(nextVersion)))
                    .andExpect(status().isOk());
        }
    }

    private String publishAnalysis(Staff staff, String entryId, String patternId, String verbClass, String vowel) throws Exception {
        String draft = createAnalysis(staff.editor, entryId, patternId, verbClass, vowel);
        String submitted = act(staff.editor, "/api/v1/admin/morphology/analyses/" + id(draft) + "/submit", draft);
        String verified = act(staff.reviewer, "/api/v1/admin/morphology/analyses/" + id(draft) + "/verify", submitted);
        act(staff.publisher, "/api/v1/admin/morphology/analyses/" + id(draft) + "/publish", verified);
        return entryId;
    }

    private String createAnalysis(String token, String entryId, String patternId, String verbClass, String vowel) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/morphology/analyses")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(analysisBody(entryId, patternId, verbClass, vowel)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private static String analysisBody(String entryId, String patternId, String verbClass, String vowel) {
        String vowelJson = vowel == null ? "null" : "\"" + vowel + "\"";
        String classJson = verbClass == null ? "null" : "\"" + verbClass + "\"";
        return """
                {"lexicalEntryId":"%s","patternId":"%s","derivation":"ROOT_DERIVED","verbClass":%s,"imperfectVowel":%s,"notes":"باب موثّق للاختبار"}
                """.formatted(entryId, patternId, classJson, vowelJson);
    }

    private String publishRoot(Staff staff, String original) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/admin/dictionary/roots")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"original":"%s"}
                                """.formatted(original)))
                .andReturn();
        if (created.getResponse().getStatus() == 409) {
            String existing = jdbc.queryForObject("select id::text from linguistic_root where root_normalized = ?", String.class, original);
            return "{\"data\":{\"id\":\"" + existing + "\",\"version\":0}}";
        }
        assertThat(created.getResponse().getStatus()).isEqualTo(200);
        String root = body(created);
        root = act(staff.editor, "/api/v1/admin/dictionary/roots/" + id(root) + "/submit", root);
        root = act(staff.reviewer, "/api/v1/admin/dictionary/roots/" + id(root) + "/verify", root);
        return act(staff.publisher, "/api/v1/admin/dictionary/roots/" + id(root) + "/publish", root);
    }

    private String citation(Staff staff) throws Exception {
        String source = body(mockMvc.perform(post("/api/v1/admin/sources")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceType":"DICTIONARY","title":"مقاييس اللغة","author":"ابن فارس","edition":"اختبار","licenseType":"PUBLIC_DOMAIN","publicDomain":true,"attributionText":"اختبار"}
                                """))
                .andExpect(status().isOk())
                .andReturn());
        source = act(staff.editor, "/api/v1/admin/sources/" + id(source) + "/submit", source);
        source = act(staff.reviewer, "/api/v1/admin/sources/" + id(source) + "/verify", source);
        act(staff.publisher, "/api/v1/admin/sources/" + id(source) + "/publish", source);
        return body(mockMvc.perform(post("/api/v1/admin/sources/" + id(source) + "/citations")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pageFrom":1,"pageTo":2,"entryLabel":"اختبار"}
                                """))
                .andExpect(status().isOk())
                .andReturn());
    }

    private String publishEntry(Staff staff, String lemma, String vocalized, String rootId, String pos, String definition, String citation) throws Exception {
        String vocalizedJson = vocalized == null ? "null" : "\"" + vocalized + "\"";
        String entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lemma":"%s","vocalizedForm":%s,"rootId":"%s","partOfSpeech":"%s","gender":"MASCULINE"}
                                """.formatted(lemma, vocalizedJson, rootId, pos)))
                .andExpect(status().isOk())
                .andReturn());
        entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/senses")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"sense":{"definition":"%s","shortDefinition":"%s","displayOrder":1}}
                                """.formatted(version(entry), definition, definition)))
                .andExpect(status().isOk())
                .andReturn());
        entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/senses/" + JsonPath.read(entry, "$.data.senses[0].id") + "/citations")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"citationId":"%s"}
                                """.formatted(version(entry), id(citation))))
                .andExpect(status().isOk())
                .andReturn());
        entry = act(staff.editor, "/api/v1/admin/dictionary/entries/" + id(entry) + "/submit", entry);
        entry = act(staff.reviewer, "/api/v1/admin/dictionary/entries/" + id(entry) + "/verify", entry);
        return act(staff.publisher, "/api/v1/admin/dictionary/entries/" + id(entry) + "/publish", entry);
    }

    private String addForm(String token, String entry, String type, String form) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/forms")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"form":{"formType":"%s","originalForm":"%s"}}
                                """.formatted(version(entry), type, form)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private String publishEntryAgain(Staff staff, String entry) throws Exception {
        entry = act(staff.editor, "/api/v1/admin/dictionary/entries/" + id(entry) + "/submit", entry);
        entry = act(staff.reviewer, "/api/v1/admin/dictionary/entries/" + id(entry) + "/verify", entry);
        return act(staff.publisher, "/api/v1/admin/dictionary/entries/" + id(entry) + "/publish", entry);
    }

    private Staff staff() throws Exception {
        Tokens owner = login("owner", "Owner-Pass-123!");
        int sequence = SEQUENCE.incrementAndGet();
        return new Staff(
                owner.accessToken(),
                member(owner, "s3e" + sequence, EDITOR_ROLE, "Editor-Pass-123!"),
                member(owner, "s3r" + sequence, REVIEWER_ROLE, "Review-Pass-123!"),
                member(owner, "s3p" + sequence, PUBLISHER_ROLE, "Publish-Pass-123!"),
                member(owner, "s3a" + sequence, AUDITOR_ROLE, "Audit-Pass-123!"));
    }

    private String member(Tokens owner, String username, UUID role, String password) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/admin/users")
                        .header("Authorization", bearer(owner.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s@arabic-reference.test","displayName":"%s","roleIds":["%s"]}
                                """.formatted(username, username, username, role)))
                .andExpect(status().isOk())
                .andReturn();
        String temporary = JsonPath.read(created.getResponse().getContentAsString(), "$.data.temporaryPassword");
        return changePassword(login(username, temporary), temporary, password).accessToken();
    }

    private String act(String token, String path, String resource) throws Exception {
        return body(mockMvc.perform(post(path)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(resource))))
                .andExpect(status().isOk())
                .andReturn());
    }

    private Tokens login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();
        String payload = result.getResponse().getContentAsString();
        return new Tokens(JsonPath.read(payload, "$.data.accessToken"), JsonPath.read(payload, "$.data.refreshToken"));
    }

    private Tokens changePassword(Tokens current, String currentPassword, String newPassword) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/change-password")
                        .header("Authorization", bearer(current.accessToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"%s","newPassword":"%s"}
                                """.formatted(currentPassword, newPassword)))
                .andExpect(status().isOk())
                .andReturn();
        String payload = result.getResponse().getContentAsString();
        return new Tokens(JsonPath.read(payload, "$.data.accessToken"), JsonPath.read(payload, "$.data.refreshToken"));
    }

    private void race(int threads, Runnable action) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?>[] futures = new Future<?>[threads];
            for (int index = 0; index < threads; index++) {
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

    private int statusOf(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        try {
            return mockMvc.perform(request).andReturn().getResponse().getStatus();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
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

    private static String versionBody(long version) {
        return "{\"version\":" + version + "}";
    }

    private static String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString();
    }

    private static String id(String body) {
        return JsonPath.read(body, "$.data.id");
    }

    private static int ruleVersion(String body, String code) {
        net.minidev.json.JSONArray versions = JsonPath.read(body, "$.data[?(@.code == '" + code + "')].version");
        return ((Number) versions.get(0)).intValue();
    }

    private static long version(String body) {
        return ((Number) JsonPath.read(body, "$.data.version")).longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private record Tokens(String accessToken, String refreshToken) {
    }

    private record Staff(String owner, String editor, String reviewer, String publisher, String auditor) {
    }
}
