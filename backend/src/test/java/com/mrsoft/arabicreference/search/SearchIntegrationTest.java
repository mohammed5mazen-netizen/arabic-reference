package com.mrsoft.arabicreference.search;

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
class SearchIntegrationTest {

    private static final UUID EDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000003");
    private static final UUID REVIEWER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000004");
    private static final UUID PUBLISHER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000005");
    private static final UUID AUDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(400);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.rate-limit.login", () -> "1000");
        registry.add("app.admin.rate-limit.refresh", () -> "1000");
        registry.add("app.search.rate-limit-per-minute", () -> "1000");
        registry.add("app.morphology.rate-limit-per-minute", () -> "1000");
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
    void unifiedSearchRanksPublishedKnowledgeOnly() throws Exception {
        Integer version6 = jdbc.queryForObject("select count(*) from flyway_schema_history where success = true and version = '6'", Integer.class);
        assertThat(version6).isEqualTo(1);
        mockMvc.perform(get("/api/v1/public/search").param("q", " ")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/search").param("q", "ك")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/search").param("q", "ك".repeat(121))).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/search").param("q", "كتاب")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/search/suggestions").param("q", "كت")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/admin/search/reindex")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/search/status")).andExpect(status().isUnauthorized());

        Staff staff = staff();
        mockMvc.perform(get("/api/v1/admin/search/status").header("Authorization", bearer(staff.auditor))).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/search/reindex").header("Authorization", bearer(staff.auditor))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/search/status").header("Authorization", bearer(staff.owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.indexVersion").value(2));

        int mark = SEQUENCE.incrementAndGet();
        String exact = word("كتب", mark);
        String defined = word("دفت", mark);
        String phrase = "الشخص الذي يكتب" + arabicMark(mark);
        String form = word("كتا", mark + 17);
        String alias = word("علإ", mark);
        String root = root(mark);
        String citation = citation(staff);
        String publishedRoot = publishRoot(staff, root);
        String entry = publishEntry(staff, exact, "كِتَاب", id(publishedRoot), "NOUN", "اسم للمكتوب " + exact, citation);
        entry = addForm(staff.editor, entry, "PLURAL", form);
        entry = republish(staff, entry);
        String meaning = publishEntry(staff, defined, null, null, "NOUN", phrase, citation);
        String concept = createConcept(staff.editor, "فاعل" + arabicMark(mark), "اسم مرفوع", "الفاعل هو الاسم المرفوع");
        concept = body(mockMvc.perform(post("/api/v1/admin/grammar/concepts/" + id(concept) + "/aliases").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"alias":"%s"}
                """.formatted(version(concept), alias))).andExpect(status().isOk()).andReturn());
        concept = body(mockMvc.perform(post("/api/v1/admin/grammar/concepts/" + id(concept) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(concept), id(citation)))).andExpect(status().isOk()).andReturn());
        concept = publishConcept(staff, concept);
        publishEntry(staff, "كتاب", "كِتَاب", id(publishedRoot), "NOUN", "ما يكتب فيه", citation);
        mockMvc.perform(get("/api/v1/public/search").param("q", "والكتاب"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.matchReason == 'MORPHOLOGY')]").isNotEmpty());

        MvcResult exactResult = mockMvc.perform(get("/api/v1/public/search").param("q", exact).param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(id(entry)))
                .andExpect(jsonPath("$.data.items[0].matchReason").value("EXACT"))
                .andExpect(jsonPath("$.data.items[0].score").doesNotExist())
                .andExpect(jsonPath("$.data.items[0].highlights").isArray())
                .andReturn();
        String exactBody = exactResult.getResponse().getContentAsString();
        int exactIndex = exactBody.indexOf(id(entry));
        int meaningIndex = exactBody.indexOf(id(meaning));
        if (meaningIndex >= 0) {
            assertThat(exactIndex).isLessThan(meaningIndex);
        }
        mockMvc.perform(get("/api/v1/public/search").param("q", exact).param("size", "20"))
                .andExpect(jsonPath("$.data.items[0].id").value(id(entry)));
        String vocalized = exact.substring(0, exact.offsetByCodePoints(0, 1)) + "\u0650" + exact.substring(exact.offsetByCodePoints(0, 1));
        mockMvc.perform(get("/api/v1/public/search").param("q", vocalized))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].matchReason").value("NORMALIZED_EXACT"));
        mockMvc.perform(get("/api/v1/public/search").param("q", form))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id == '" + id(entry) + "')].matchReason").value(org.hamcrest.Matchers.hasItem("WORD_FORM")));
        mockMvc.perform(get("/api/v1/public/search").param("q", root))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.type == 'ROOT')].title").value(org.hamcrest.Matchers.hasItem(root)));
        mockMvc.perform(get("/api/v1/public/search").param("q", alias))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id == '" + id(concept) + "')].matchReason").value(org.hamcrest.Matchers.hasItem("ALIAS")));
        mockMvc.perform(get("/api/v1/public/search").param("q", phrase))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id == '" + id(meaning) + "')].matchReason").value(org.hamcrest.Matchers.hasItem("DEFINITION")));
        mockMvc.perform(get("/api/v1/public/search").param("q", exact.substring(0, exact.offsetByCodePoints(0, 3))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id == '" + id(entry) + "')]").isNotEmpty());
        mockMvc.perform(get("/api/v1/public/search").param("q", exact).param("type", "dictionary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.type == 'ROOT')]").isEmpty());
        mockMvc.perform(get("/api/v1/public/search").param("q", exact).param("type", "dictionary").param("partOfSpeech", "VERB"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.id == '" + id(entry) + "')]").isEmpty());
        mockMvc.perform(get("/api/v1/public/search").param("q", exact).param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.page").value(1));
        mockMvc.perform(get("/api/v1/public/search").param("q", "kitab")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/search").param("q", "zzzzzzzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(org.hamcrest.Matchers.lessThanOrEqualTo(2)));
        mockMvc.perform(get("/api/v1/public/search").param("q", "<script>alert(1)</script>"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.query").value("<script>alert(1)</script>"));
        mockMvc.perform(get("/api/v1/public/search/suggestions").param("q", exact.substring(0, exact.offsetByCodePoints(0, 2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(8)));

        String draft = body(mockMvc.perform(patch("/api/v1/admin/dictionary/entries/" + id(entry)).header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"draft":{"lemma":"%s","vocalizedForm":null,"rootId":"%s","partOfSpeech":"NOUN","gender":"MASCULINE"}}
                """.formatted(version(entry), word("مسو", mark), id(publishedRoot)))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(get("/api/v1/public/search").param("q", exact))
                .andExpect(jsonPath("$.data.items[?(@.id == '" + id(entry) + "')].title").value(org.hamcrest.Matchers.hasItem(exact)));
        String archived = republish(staff, draft);
        archived = act(staff.publisher, "/api/v1/admin/dictionary/entries/" + id(archived) + "/archive", archived);
        mockMvc.perform(get("/api/v1/public/search").param("q", word("مسو", mark)))
                .andExpect(jsonPath("$.data.items[?(@.id == '" + id(entry) + "')]").isEmpty());

        mockMvc.perform(post("/api/v1/admin/search/reindex").header("Authorization", bearer(staff.owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consistent").value(true));
        jdbc.update("delete from search_document where entity_type = 'GRAMMAR_CONCEPT' and entity_id = ?::uuid", id(concept));
        mockMvc.perform(get("/api/v1/admin/search/status").header("Authorization", bearer(staff.owner)))
                .andExpect(jsonPath("$.data.consistent").value(false))
                .andExpect(jsonPath("$.data.missingDocuments").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
        mockMvc.perform(post("/api/v1/admin/search/repair").header("Authorization", bearer(staff.owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.consistent").value(true));
        Integer repairs = jdbc.queryForObject("select count(*) from admin_audit_event where event_type = 'SEARCH_INDEX_REPAIR'", Integer.class);
        assertThat(repairs).isGreaterThan(0);
    }

    @Test
    void concurrentRebuildsDoNotBothSucceed() throws Exception {
        Staff staff = staff();
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<?>[] futures = new Future<?>[2];
            for (int index = 0; index < 2; index++) {
                futures[index] = executor.submit(() -> {
                    try {
                        start.await();
                        int statusCode = mockMvc.perform(post("/api/v1/admin/search/reindex").header("Authorization", bearer(staff.owner))).andReturn().getResponse().getStatus();
                        if (statusCode == 200) {
                            success.incrementAndGet();
                        } else if (statusCode == 409) {
                            conflict.incrementAndGet();
                        } else {
                            throw new AssertionError("Unexpected status " + statusCode);
                        }
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                    } catch (Exception exception) {
                        throw new IllegalStateException(exception);
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
        assertThat(success.get()).isEqualTo(1);
        assertThat(conflict.get()).isEqualTo(1);
    }

    private String publishRoot(Staff staff, String original) throws Exception {
        String root = body(mockMvc.perform(post("/api/v1/admin/dictionary/roots").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"original":"%s"}
                """.formatted(original))).andExpect(status().isOk()).andReturn());
        root = act(staff.editor, "/api/v1/admin/dictionary/roots/" + id(root) + "/submit", root);
        root = act(staff.reviewer, "/api/v1/admin/dictionary/roots/" + id(root) + "/verify", root);
        return act(staff.publisher, "/api/v1/admin/dictionary/roots/" + id(root) + "/publish", root);
    }

    private String publishEntry(Staff staff, String lemma, String vocalized, String rootId, String pos, String definition, String citation) throws Exception {
        String vocalizedJson = vocalized == null ? "null" : "\"" + vocalized + "\"";
        String rootJson = rootId == null ? "null" : "\"" + rootId + "\"";
        String entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"lemma":"%s","vocalizedForm":%s,"rootId":%s,"partOfSpeech":"%s","gender":"MASCULINE"}
                """.formatted(lemma, vocalizedJson, rootJson, pos))).andExpect(status().isOk()).andReturn());
        entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/senses").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"sense":{"definition":"%s","shortDefinition":"%s","displayOrder":1}}
                """.formatted(version(entry), definition, definition))).andExpect(status().isOk()).andReturn());
        entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/senses/" + JsonPath.read(entry, "$.data.senses[0].id") + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(entry), id(citation)))).andExpect(status().isOk()).andReturn());
        entry = act(staff.editor, "/api/v1/admin/dictionary/entries/" + id(entry) + "/submit", entry);
        entry = act(staff.reviewer, "/api/v1/admin/dictionary/entries/" + id(entry) + "/verify", entry);
        return act(staff.publisher, "/api/v1/admin/dictionary/entries/" + id(entry) + "/publish", entry);
    }

    private String republish(Staff staff, String entry) throws Exception {
        entry = act(staff.editor, "/api/v1/admin/dictionary/entries/" + id(entry) + "/submit", entry);
        entry = act(staff.reviewer, "/api/v1/admin/dictionary/entries/" + id(entry) + "/verify", entry);
        return act(staff.publisher, "/api/v1/admin/dictionary/entries/" + id(entry) + "/publish", entry);
    }

    private String addForm(String token, String entry, String type, String form) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/forms").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"form":{"formType":"%s","originalForm":"%s"}}
                """.formatted(version(entry), type, form))).andExpect(status().isOk()).andReturn());
    }

    private String citation(Staff staff) throws Exception {
        String source = body(mockMvc.perform(post("/api/v1/admin/sources").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"sourceType":"DICTIONARY","title":"مقاييس اللغة","author":"ابن فارس","edition":"بحث","licenseType":"PUBLIC_DOMAIN","publicDomain":true,"attributionText":"بحث"}
                """)).andExpect(status().isOk()).andReturn());
        source = act(staff.editor, "/api/v1/admin/sources/" + id(source) + "/submit", source);
        source = act(staff.reviewer, "/api/v1/admin/sources/" + id(source) + "/verify", source);
        act(staff.publisher, "/api/v1/admin/sources/" + id(source) + "/publish", source);
        return body(mockMvc.perform(post("/api/v1/admin/sources/" + id(source) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"pageFrom":1,"pageTo":2,"entryLabel":"بحث"}
                """)).andExpect(status().isOk()).andReturn());
    }

    private String createConcept(String token, String term, String shortDefinition, String detailed) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/grammar/concepts").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"term":"%s","shortDefinition":"%s","detailedDefinition":"%s"}
                """.formatted(term, shortDefinition, detailed))).andExpect(status().isOk()).andReturn());
    }

    private String publishConcept(Staff staff, String concept) throws Exception {
        concept = act(staff.editor, "/api/v1/admin/grammar/concepts/" + id(concept) + "/submit", concept);
        concept = act(staff.reviewer, "/api/v1/admin/grammar/concepts/" + id(concept) + "/verify", concept);
        return act(staff.publisher, "/api/v1/admin/grammar/concepts/" + id(concept) + "/publish", concept);
    }

    private Staff staff() throws Exception {
        Tokens owner = login("owner", "Owner-Pass-123!");
        int sequence = SEQUENCE.incrementAndGet();
        return new Staff(owner.accessToken(), member(owner, "s5e" + sequence, EDITOR_ROLE, "Editor-Pass-123!"), member(owner, "s5r" + sequence, REVIEWER_ROLE, "Review-Pass-123!"), member(owner, "s5p" + sequence, PUBLISHER_ROLE, "Publish-Pass-123!"), member(owner, "s5a" + sequence, AUDITOR_ROLE, "Audit-Pass-123!"));
    }

    private String member(Tokens owner, String username, UUID role, String password) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(owner.accessToken())).contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"%s","email":"%s@arabic-reference.test","displayName":"%s","roleIds":["%s"]}
                """.formatted(username, username, username, role))).andExpect(status().isOk()).andReturn();
        String temporary = JsonPath.read(created.getResponse().getContentAsString(), "$.data.temporaryPassword");
        return changePassword(login(username, temporary), temporary, password).accessToken();
    }

    private String act(String token, String path, String resource) throws Exception {
        return body(mockMvc.perform(post(path).header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("{\"version\":" + version(resource) + "}")).andExpect(status().isOk()).andReturn());
    }

    private Tokens login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"%s","password":"%s"}
                """.formatted(username, password))).andExpect(status().isOk()).andReturn();
        String payload = result.getResponse().getContentAsString();
        return new Tokens(JsonPath.read(payload, "$.data.accessToken"), JsonPath.read(payload, "$.data.refreshToken"));
    }

    private Tokens changePassword(Tokens current, String currentPassword, String newPassword) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/admin/auth/change-password").header("Authorization", bearer(current.accessToken())).contentType(MediaType.APPLICATION_JSON).content("""
                {"currentPassword":"%s","newPassword":"%s"}
                """.formatted(currentPassword, newPassword))).andExpect(status().isOk()).andReturn();
        String payload = result.getResponse().getContentAsString();
        return new Tokens(JsonPath.read(payload, "$.data.accessToken"), JsonPath.read(payload, "$.data.refreshToken"));
    }

    private static String word(String stem, int mark) {
        return stem + arabicMark(mark);
    }

    private static String root(int mark) {
        String letters = "بتثجحخدذرزسشصضطظعغفقكلمنهوي";
        return "" + letters.charAt(mark % letters.length()) + letters.charAt((mark / letters.length()) % letters.length()) + letters.charAt((mark / (letters.length() * letters.length()) + 3) % letters.length());
    }

    private static String arabicMark(int mark) {
        String letters = "بتثجحخدذرزسشصضطظعغفقكلمنهوي";
        StringBuilder builder = new StringBuilder();
        int value = Math.max(mark, 1);
        do {
            builder.append(letters.charAt(value % letters.length()));
            value /= letters.length();
        } while (value > 0);
        return builder.toString();
    }

    private static String body(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString();
    }

    private static String id(String body) {
        return JsonPath.read(body, "$.data.id");
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
