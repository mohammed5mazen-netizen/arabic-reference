package com.mrsoft.arabicreference.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mrsoft.arabicreference.IntegrationContainers;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
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
class DictionaryIntegrationTest {

    static final UUID EDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000003");
    static final UUID REVIEWER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000004");
    static final UUID PUBLISHER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000005");
    static final UUID AUDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    static final String OWNER_PASSWORD = "Owner-Pass-123!";
    static final AtomicInteger STAFF_SEQUENCE = new AtomicInteger();

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.rate-limit.login", () -> "1000");
        registry.add("app.admin.rate-limit.refresh", () -> "1000");
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> OWNER_PASSWORD);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void flywayAddsDictionaryPermissionsWithoutEditingEarlierMigrations() {
        Integer version3 = jdbc.queryForObject(
                "select count(*) from flyway_schema_history where success = true and version = '3'", Integer.class);
        Integer permissions = jdbc.queryForObject("select count(*) from admin_permission", Integer.class);
        assertThat(version3).isEqualTo(1);
        assertThat(permissions).isEqualTo(PermissionCatalog.all().size());
        assertThat(jdbc.queryForObject(
                "select count(*) from pg_indexes where indexname = 'ix_lexical_entry_published_lemma'", Integer.class)).isEqualTo(1);
    }

    @Test
    void anonymousReadsAreOpenAndAdminDictionaryIsClosed() throws Exception {
        mockMvc.perform(get("/api/v1/public/dictionary/lookup").param("word", "كتاب"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
        mockMvc.perform(get("/api/v1/public/dictionary/lookup").param("word", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mockMvc.perform(get("/api/v1/public/dictionary/lookup").param("word", "ا".repeat(81)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/dictionary/lookup").param("word", "book"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/dictionary/entries/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mockMvc.perform(get("/api/v1/admin/dictionary/entries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void editorialWorkflowPublishesOnlyCitedAttributableContent() throws Exception {
        Staff staff = staff();
        mockMvc.perform(get("/api/v1/admin/dictionary/entries").header("Authorization", bearer(staff.auditor)))
                .andExpect(status().isForbidden());

        String root = createRoot(staff.editor, "كتب", null);
        publishRoot(staff, root);
        String source = createSource(staff.editor, "PUBLIC_DOMAIN", true, "ابن فارس، مقاييس اللغة");
        publishSource(staff, source);
        String citation = cite(staff.editor, id(source), 120, 121);

        String noun = createEntry(staff.editor, "كتاب", "كِتَاب", id(root), "NOUN");
        String verb = createEntry(staff.editor, "كتاب", null, null, "VERB");
        assertThat(id(noun)).isNotEqualTo(id(verb));

        String uncited = createEntry(staff.editor, "دفتر", null, null, "NOUN");
        uncited = addSense(staff.editor, uncited, "مجموعة أوراق", "أوراق", 1);
        uncited = submitEntry(staff.editor, uncited);
        uncited = verifyEntry(staff.owner, uncited);
        mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(uncited) + "/publish")
                        .header("Authorization", bearer(staff.owner))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(uncited))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(uncited) + "/publish")
                        .header("Authorization", bearer(staff.publisher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(uncited))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("citation")));
        mockMvc.perform(get("/api/v1/public/dictionary/entries/" + id(uncited)))
                .andExpect(status().isNotFound());

        noun = addSense(staff.editor, noun, "ما يُكتب فيه", "صحيفة مجموعة", 1);
        noun = linkSense(staff.editor, noun, citation);
        noun = addForm(staff.editor, noun, "PLURAL", "كُتُب");
        noun = addExample(staff.editor, noun, "EDITORIAL", "قرأت الكتاب");
        mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(noun) + "/verify")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(noun))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(noun) + "/publish")
                        .header("Authorization", bearer(staff.publisher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(noun))))
                .andExpect(status().isConflict());

        String derived = createEntry(staff.editor, "كاتب", "كَاتِب", id(root), "NOUN");
        derived = addSense(staff.editor, derived, "من يكتب", "من يكتب", 1);
        derived = linkSense(staff.editor, derived, citation);
        derived = submitEntry(staff.editor, derived);
        derived = verifyEntry(staff.reviewer, derived);
        derived = publishEntry(staff.publisher, derived);

        noun = addRelation(staff.editor, noun, "DERIVED_FROM", id(derived), null, null);
        noun = submitEntry(staff.editor, noun);
        noun = verifyEntry(staff.reviewer, noun);
        mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(noun) + "/publish")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(noun))))
                .andExpect(status().isForbidden());
        noun = publishEntry(staff.publisher, noun);

        String definition = jdbc.queryForObject("select definition from lexical_sense where lexical_entry_id = ?", String.class, UUID.fromString(id(noun)));
        String metadata = jdbc.queryForObject(
                "select coalesce(string_agg(metadata::text, ' '), '') from admin_audit_event where event_type in ('DICTIONARY_ENTRY_CREATED','SENSE_ADDED','CONTENT_PUBLISHED')",
                String.class);
        assertThat(metadata).doesNotContain(definition);
        assertThat(jdbc.queryForObject(
                "select count(*) from admin_audit_event where event_type = 'CONTENT_PUBLISHED' and target_id = ?",
                Integer.class,
                id(noun))).isEqualTo(1);

        mockMvc.perform(get("/api/v1/public/dictionary/lookup").param("word", "كِتَاب"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].lemmaOriginal").value("كتاب"))
                .andExpect(jsonPath("$.data.items[0].vocalizedForm").value("كِتَاب"))
                .andExpect(jsonPath("$.data.items[0].root").value("كتب"));
        mockMvc.perform(get("/api/v1/public/dictionary/by-slug/" + slug(noun)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.senses[0].sources[0].title").value("مقاييس اللغة"))
                .andExpect(jsonPath("$.data.senses[0].sources[0].author").value("ابن فارس"))
                .andExpect(jsonPath("$.data.relations[0].relationType").value("DERIVED_FROM"))
                .andExpect(jsonPath("$.data.createdBy").doesNotExist());
        mockMvc.perform(get("/api/v1/public/dictionary/roots/كتب"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.original").value("كتب"))
                .andExpect(jsonPath("$.data.entries.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));

        Statistics statistics = sessionFactory.getStatistics();
        statistics.setStatisticsEnabled(true);
        statistics.clear();
        mockMvc.perform(get("/api/v1/public/dictionary/by-slug/" + slug(noun))).andExpect(status().isOk());
        assertThat(statistics.getPrepareStatementCount()).isBetween(1L, 4L);

        noun = updateLemma(staff.editor, noun, "كتاب");
        mockMvc.perform(get("/api/v1/admin/dictionary/entries/" + id(noun) + "/revisions")
                        .header("Authorization", bearer(staff.editor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
        mockMvc.perform(get("/api/v1/public/dictionary/by-slug/" + slug(noun)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lemmaOriginal").value("كتاب"));

        String restricted = createSource(staff.editor, "RESTRICTED", false, "لا يُنشر");
        restricted = submitSource(staff.editor, restricted);
        restricted = verifySource(staff.reviewer, restricted);
        mockMvc.perform(post("/api/v1/admin/sources/" + id(restricted) + "/publish")
                        .header("Authorization", bearer(staff.publisher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(restricted))))
                .andExpect(status().isForbidden());
        String unknown = createSource(staff.editor, "UNKNOWN", false, "مصدر غير محدد");
        unknown = submitSource(staff.editor, unknown);
        unknown = verifySource(staff.reviewer, unknown);
        mockMvc.perform(post("/api/v1/admin/sources/" + id(unknown) + "/publish")
                        .header("Authorization", bearer(staff.publisher))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(unknown))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/public/dictionary/lookup").param("word", "دفتر"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void duplicateRelationAndStaleVersionAreConflicts() throws Exception {
        Staff staff = staff();
        String left = createEntry(staff.editor, "عين", null, null, "NOUN");
        String right = createEntry(staff.editor, "بصر", null, null, "NOUN");
        left = addSense(staff.editor, left, "حاسة البصر", "حاسة", 1);
        right = addSense(staff.editor, right, "حاسة النظر", "نظر", 1);
        String leftSense = senseId(left);
        String rightSense = senseId(right);
        left = addRelation(staff.editor, left, "SYNONYM", id(right), leftSense, rightSense);
        mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(left) + "/relations")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"relation":{"relationType":"SYNONYM","targetEntryId":"%s","sourceSenseId":"%s","targetSenseId":"%s"}}
                                """.formatted(version(left), id(right), leftSense, rightSense)))
                .andExpect(status().isConflict());
        mockMvc.perform(patch("/api/v1/admin/dictionary/entries/" + id(left))
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":0,"draft":{"lemma":"عين","partOfSpeech":"NOUN"}}
                                """))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/v1/admin/sources/" + UUID.randomUUID() + "/citations")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pageFrom":3,"pageTo":1}
                                """))
                .andExpect(status().isNotFound());
        String source = createSource(staff.editor, "CC_BY", false, "إسناد");
        mockMvc.perform(post("/api/v1/admin/sources/" + id(source) + "/citations")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pageFrom":8,"pageTo":2}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/sources")
                        .header("Authorization", bearer(staff.editor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"مجهول","licenseType":"NOT_A_LICENSE","attributionText":"لا"}
                                """))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/dictionary/entries").param("size", "51").header("Authorization", bearer(staff.editor)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void concurrentEditsAndTransitionsStaySingleWinner() throws Exception {
        Staff staff = staff();
        String created = createEntry(staff.editor, "قلم", null, null, "NOUN");
        String entryId = id(created);
        long current = version(created);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        race(2, () -> {
            int statusCode = statusOf(patch("/api/v1/admin/dictionary/entries/" + entryId)
                    .header("Authorization", bearer(staff.editor))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"version":%d,"draft":{"lemma":"قلم","partOfSpeech":"NOUN"}}
                            """.formatted(current)));
            count(statusCode, success, conflict);
        });
        assertThat(success.get()).isEqualTo(1);
        assertThat(conflict.get()).isEqualTo(1);

        String withSense = addSense(staff.editor, reload(staff.editor, entryId), "أداة الكتابة", "أداة", 1);
        String sense = senseId(withSense);
        String other = addSense(staff.editor, createEntry(staff.editor, "حبر", null, null, "NOUN"), "مداد", "مداد", 1);
        long relationVersion = version(withSense);
        String relationBody = """
                {"version":%d,"relation":{"relationType":"RELATED","targetEntryId":"%s","sourceSenseId":"%s","targetSenseId":"%s"}}
                """.formatted(relationVersion, id(other), sense, senseId(other));
        AtomicInteger relationSuccess = new AtomicInteger();
        AtomicInteger relationConflict = new AtomicInteger();
        race(2, () -> count(statusOf(post("/api/v1/admin/dictionary/entries/" + entryId + "/relations")
                .header("Authorization", bearer(staff.editor))
                .contentType(MediaType.APPLICATION_JSON)
                .content(relationBody)), relationSuccess, relationConflict));
        assertThat(relationSuccess.get()).isEqualTo(1);
        assertThat(relationConflict.get()).isEqualTo(1);

        String submitted = submitEntry(staff.editor, reload(staff.editor, entryId));
        long reviewVersion = version(submitted);
        AtomicInteger reviewSuccess = new AtomicInteger();
        AtomicInteger reviewConflict = new AtomicInteger();
        race(2, () -> count(statusOf(post("/api/v1/admin/dictionary/entries/" + entryId + "/verify")
                .header("Authorization", bearer(staff.reviewer))
                .contentType(MediaType.APPLICATION_JSON)
                .content(versionBody(reviewVersion))), reviewSuccess, reviewConflict));
        assertThat(reviewSuccess.get()).isEqualTo(1);
        assertThat(reviewConflict.get()).isEqualTo(1);
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

    private Staff staff() throws Exception {
        Tokens owner = login("owner", OWNER_PASSWORD);
        int sequence = STAFF_SEQUENCE.incrementAndGet();
        return new Staff(
                owner.accessToken(),
                member(owner, "s2e" + sequence, EDITOR_ROLE, "Editor-Pass-123!"),
                member(owner, "s2r" + sequence, REVIEWER_ROLE, "Review-Pass-123!"),
                member(owner, "s2p" + sequence, PUBLISHER_ROLE, "Publish-Pass-123!"),
                member(owner, "s2a" + sequence, AUDITOR_ROLE, "Audit-Pass-123!"));
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

    private String createRoot(String token, String original, String notes) throws Exception {
        String notesJson = notes == null ? "null" : "\"" + notes + "\"";
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/roots")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"original":"%s","notes":%s}
                                """.formatted(original, notesJson)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private void publishRoot(Staff staff, String root) throws Exception {
        root = act(staff.editor, "/api/v1/admin/dictionary/roots/" + id(root) + "/submit", root);
        root = act(staff.reviewer, "/api/v1/admin/dictionary/roots/" + id(root) + "/verify", root);
        act(staff.publisher, "/api/v1/admin/dictionary/roots/" + id(root) + "/publish", root);
    }

    private String createSource(String token, String license, boolean publicDomain, String attribution) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/sources")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sourceType":"DICTIONARY","title":"مقاييس اللغة","author":"ابن فارس","edition":"الأولى","publicationYear":1979,"licenseType":"%s","publicDomain":%s,"attributionText":"%s"}
                                """.formatted(license, publicDomain, attribution)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private void publishSource(Staff staff, String source) throws Exception {
        source = submitSource(staff.editor, source);
        source = verifySource(staff.reviewer, source);
        act(staff.publisher, "/api/v1/admin/sources/" + id(source) + "/publish", source);
    }

    private String submitSource(String token, String source) throws Exception {
        return act(token, "/api/v1/admin/sources/" + id(source) + "/submit", source);
    }

    private String verifySource(String token, String source) throws Exception {
        return act(token, "/api/v1/admin/sources/" + id(source) + "/verify", source);
    }

    private String cite(String token, String sourceId, int from, int to) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/sources/" + sourceId + "/citations")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pageFrom":%d,"pageTo":%d,"entryLabel":"كتب"}
                                """.formatted(from, to)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private String createEntry(String token, String lemma, String vocalized, String rootId, String pos) throws Exception {
        String vocalizedJson = vocalized == null ? "null" : "\"" + vocalized + "\"";
        String rootJson = rootId == null ? "null" : "\"" + rootId + "\"";
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lemma":"%s","vocalizedForm":%s,"rootId":%s,"partOfSpeech":"%s","gender":"MASCULINE"}
                                """.formatted(lemma, vocalizedJson, rootJson, pos)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private String addSense(String token, String entry, String definition, String shortDefinition, int order) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/senses")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"sense":{"definition":"%s","shortDefinition":"%s","usageLabel":"CLASSICAL","domainLabel":"LANGUAGE","displayOrder":%d}}
                                """.formatted(version(entry), definition, shortDefinition, order)))
                .andExpect(status().isOk())
                .andReturn());
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

    private String addExample(String token, String entry, String kind, String text) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/senses/" + senseId(entry) + "/examples")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"example":{"kind":"%s","text":"%s","displayOrder":1}}
                                """.formatted(version(entry), kind, text)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private String addRelation(String token, String entry, String type, String targetId, String sourceSense, String targetSense) throws Exception {
        String sourceSenseJson = sourceSense == null ? "null" : "\"" + sourceSense + "\"";
        String targetSenseJson = targetSense == null ? "null" : "\"" + targetSense + "\"";
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/relations")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"relation":{"relationType":"%s","targetEntryId":"%s","sourceSenseId":%s,"targetSenseId":%s,"verificationLevel":"REVIEWED"}}
                                """.formatted(version(entry), type, targetId, sourceSenseJson, targetSenseJson)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private String linkSense(String token, String entry, String citation) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/senses/" + senseId(entry) + "/citations")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"citationId":"%s"}
                                """.formatted(version(entry), id(citation))))
                .andExpect(status().isOk())
                .andReturn());
    }

    private String submitEntry(String token, String entry) throws Exception {
        return act(token, "/api/v1/admin/dictionary/entries/" + id(entry) + "/submit", entry);
    }

    private String verifyEntry(String token, String entry) throws Exception {
        return act(token, "/api/v1/admin/dictionary/entries/" + id(entry) + "/verify", entry);
    }

    private String publishEntry(String token, String entry) throws Exception {
        return act(token, "/api/v1/admin/dictionary/entries/" + id(entry) + "/publish", entry);
    }

    private String updateLemma(String token, String entry, String lemma) throws Exception {
        return body(mockMvc.perform(patch("/api/v1/admin/dictionary/entries/" + id(entry))
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d,"draft":{"lemma":"%s","vocalizedForm":"كِتَاب","rootId":"%s","partOfSpeech":"NOUN","gender":"MASCULINE"}}
                                """.formatted(version(entry), lemma, JsonPath.read(entry, "$.data.rootId"))))
                .andExpect(status().isOk())
                .andReturn());
    }

    private String reload(String token, String entryId) throws Exception {
        return body(mockMvc.perform(get("/api/v1/admin/dictionary/entries/" + entryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn());
    }

    private String act(String token, String path, String resource) throws Exception {
        return body(mockMvc.perform(post(path)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(versionBody(version(resource))))
                .andExpect(status().isOk())
                .andReturn());
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

    private static String slug(String body) {
        return JsonPath.read(body, "$.data.slug");
    }

    private static String senseId(String body) {
        return JsonPath.read(body, "$.data.senses[0].id");
    }

    private static long version(String body) {
        Number value = JsonPath.read(body, "$.data.version");
        return value.longValue();
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

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private record Tokens(String accessToken, String refreshToken) {
    }

    private record Staff(String owner, String editor, String reviewer, String publisher, String auditor) {
    }
}
