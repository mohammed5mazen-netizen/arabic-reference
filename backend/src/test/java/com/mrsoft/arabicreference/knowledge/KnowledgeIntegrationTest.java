package com.mrsoft.arabicreference.knowledge;

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
class KnowledgeIntegrationTest {

    private static final UUID EDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000003");
    private static final UUID REVIEWER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000004");
    private static final UUID PUBLISHER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000005");
    private static final UUID AUDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(900);

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
    void spellingRulesStayStructuredAndPublicAfterPublication() throws Exception {
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success = true and version in ('7','8','9','10','11')", Integer.class)).isEqualTo(5);
        mockMvc.perform(get("/api/v1/public/spelling/topics")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/spelling/topics")).andExpect(status().isUnauthorized());
        Staff staff = staff();
        mockMvc.perform(get("/api/v1/admin/spelling/topics").header("Authorization", bearer(staff.auditor))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/spelling/review").header("Authorization", bearer(staff.editor))).andExpect(status().isForbidden());

        int mark = SEQUENCE.incrementAndGet();
        String label = "ظغ" + arabicMark(mark);
        String citation = citation(staff);
        String topic = body(mockMvc.perform(post("/api/v1/admin/spelling/topics").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"موضوع %s","summary":"ملخص إملائي","displayOrder":1}
                """.formatted(label))).andExpect(status().isOk()).andReturn());
        topic = cite(staff.editor, "/api/v1/admin/spelling/topics/" + id(topic) + "/citations", topic, citation);
        topic = workflow(staff, "/api/v1/admin/spelling/topics/" + id(topic), topic);
        mockMvc.perform(get("/api/v1/public/spelling/topics/" + slug(topic))).andExpect(status().isOk()).andExpect(jsonPath("$.data.title").value("موضوع " + label));

        String rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","title":"قاعدة %s","summary":"ملخص القاعدة","coreRule":"تكتب الهمزة بحسب موضعها","difficulty":"BEGINNER"}
                """.formatted(id(topic), label))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"QUOTED","correctForm":"إن","explanation":"شاهد"}
                """.formatted(version(rule)))).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"CONTRAST","correctForm":"إن","incorrectForm":"ان","explanation":"فرق موضعي"}
                """.formatted(version(rule)))).andExpect(status().isBadRequest());
        rule = act(staff.editor, "/api/v1/admin/spelling/rules/" + id(rule) + "/submit", rule);
        rule = act(staff.owner, "/api/v1/admin/spelling/rules/" + id(rule) + "/verify", rule);
        String owned = body(mockMvc.perform(post("/api/v1/admin/spelling/rules").header("Authorization", bearer(staff.owner)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","title":"ملك %s","summary":"ملخص","coreRule":"قاعدة للمالك","difficulty":"BEGINNER"}
                """.formatted(id(topic), label))).andExpect(status().isOk()).andReturn());
        owned = act(staff.owner, "/api/v1/admin/spelling/rules/" + id(owned) + "/submit", owned);
        mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(owned) + "/verify").header("Authorization", bearer(staff.owner)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(owned)))).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/publish").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(rule)))).andExpect(status().isConflict());
    }

    @Test
    void spellingPublicationRequiresStructureAndKeepsTheSnapshot() throws Exception {
        Staff staff = staff();
        int mark = SEQUENCE.incrementAndGet();
        String label = "ضص" + arabicMark(mark);
        String citation = citation(staff);
        String topic = publishedTopic(staff, label, citation);
        String rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","title":"قاعدة %s","summary":"ملخص القاعدة","coreRule":"تكتب الهمزة بحسب موضعها","difficulty":"INTERMEDIATE"}
                """.formatted(id(topic), label))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/clauses").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"CONDITION","heading":"الشرط","body":"إذا توسطت الهمزة نظرت إلى حركتها"}
                """.formatted(version(rule)))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"CONTRAST","correctForm":"مسألة","incorrectForm":"مسأله","explanation":"التاء المربوطة هنا","contextNote":"قد تصح صورة أخرى في كلمة مختلفة"}
                """.formatted(version(rule)))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"COMMON_MISTAKE","correctForm":"مسألة","commonForm":"مسأله","reason":"خلط بين التاءين","citationId":"%s"}
                """.formatted(version(rule), id(citation)))).andExpect(status().isOk()).andReturn());
        String bare = body(mockMvc.perform(post("/api/v1/admin/spelling/rules").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","title":"ناقص %s","summary":"ملخص","coreRule":"قاعدة بلا مصدر","difficulty":"BEGINNER"}
                """.formatted(id(topic), label))).andExpect(status().isOk()).andReturn());
        bare = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(bare) + "/clauses").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"NOTE","heading":"ملاحظة","body":"ملاحظة بلا مصدر"}
                """.formatted(version(bare)))).andExpect(status().isOk()).andReturn());
        bare = act(staff.editor, "/api/v1/admin/spelling/rules/" + id(bare) + "/submit", bare);
        bare = act(staff.reviewer, "/api/v1/admin/spelling/rules/" + id(bare) + "/verify", bare);
        mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(bare) + "/publish").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(bare)))).andExpect(status().isConflict());
        bare = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(bare) + "/request-changes").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"reason":"أضف مصدرًا قبل النشر"}
                """.formatted(version(bare)))).andExpect(status().isOk()).andReturn());
        assertThat((String) JsonPath.read(bare, "$.data.status")).isEqualTo("CHANGES_REQUESTED");
        rule = cite(staff.editor, "/api/v1/admin/spelling/rules/" + id(rule) + "/citations", rule, citation);
        rule = act(staff.editor, "/api/v1/admin/spelling/rules/" + id(rule) + "/submit", rule);
        rule = act(staff.owner, "/api/v1/admin/spelling/rules/" + id(rule) + "/verify", rule);
        mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/publish").header("Authorization", bearer(staff.owner)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(rule)))).andExpect(status().isForbidden());
        rule = act(staff.publisher, "/api/v1/admin/spelling/rules/" + id(rule) + "/publish", rule);
        String originalTitle = "قاعدة " + label;
        mockMvc.perform(get("/api/v1/public/spelling/rules/" + slug(rule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value(originalTitle))
                .andExpect(jsonPath("$.data.clauses[0].heading").value("الشرط"))
                .andExpect(jsonPath("$.data.examples[?(@.kind == 'COMMON_MISTAKE')].reason").isNotEmpty());
        mockMvc.perform(get("/api/v1/public/spelling/rules/missing-rule")).andExpect(status().isNotFound());
        String edited = body(mockMvc.perform(patch("/api/v1/admin/spelling/rules/" + id(rule)).header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"title":"مسودة %s","summary":"ملخص بعد النشر","coreRule":"نص مسودة","difficulty":"ADVANCED"}
                """.formatted(version(rule), label))).andExpect(status().isOk()).andReturn());
        assertThat(slug(edited)).isEqualTo(slug(rule));
        mockMvc.perform(get("/api/v1/public/spelling/rules/" + slug(rule))).andExpect(jsonPath("$.data.title").value(originalTitle));
        Integer revisions = jdbc.queryForObject("select count(*) from content_revision where target_type = 'spelling_rule' and target_id = ?::uuid", Integer.class, id(rule));
        assertThat(revisions).isGreaterThan(0);
        Integer audits = jdbc.queryForObject("select count(*) from admin_audit_event where event_type = 'SPELLING_RULE_PUBLISHED' and target_id = ?", Integer.class, id(rule));
        assertThat(audits).isEqualTo(1);
        Integer updates = jdbc.queryForObject("select count(*) from admin_audit_event where event_type = 'SPELLING_RULE_UPDATED' and target_id = ?", Integer.class, id(rule));
        assertThat(updates).isGreaterThan(0);
        mockMvc.perform(patch("/api/v1/admin/spelling/rules/" + id(rule)).header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":1,"title":"تعارض %s","summary":"ملخص","coreRule":"نص","difficulty":"BEGINNER"}
                """.formatted(label))).andExpect(status().isConflict());
        mockMvc.perform(get("/api/v1/public/search").param("q", originalTitle).param("type", "spelling"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].type").value("SPELLING_RULE"))
                .andExpect(jsonPath("$.data.items[0].id").value(id(rule)));
    }

    @Test
    void rhetoricDevicesKeepInterpretationsAndRelations() throws Exception {
        Staff staff = staff();
        mockMvc.perform(get("/api/v1/public/rhetoric/topics")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/rhetoric/topics")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/admin/rhetoric/devices").header("Authorization", bearer(staff.auditor)).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        int mark = SEQUENCE.incrementAndGet();
        String label = "ثذ" + arabicMark(mark);
        String citation = citation(staff);
        String topic = body(mockMvc.perform(post("/api/v1/admin/rhetoric/topics").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"category":"BAYAN","title":"بيان %s","summary":"علم البيان","displayOrder":1}
                """.formatted(label))).andExpect(status().isOk()).andReturn());
        topic = cite(staff.editor, "/api/v1/admin/rhetoric/topics/" + id(topic) + "/citations", topic, citation);
        topic = workflow(staff, "/api/v1/admin/rhetoric/topics/" + id(topic), topic);
        String other = publishedDevice(staff, id(topic), "مقابل " + label, citation);
        String device = body(mockMvc.perform(post("/api/v1/admin/rhetoric/devices").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","name":"تشبيه %s","shortDefinition":"عقد مماثلة بين طرفين","detailedExplanation":"بيان وجه الشبه"}
                """.formatted(id(topic), label))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/rhetoric/devices/" + id(device) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"QUOTED","text":"شاهد","explanation":"شرح"}
                """.formatted(version(device)))).andExpect(status().isBadRequest());
        device = body(mockMvc.perform(post("/api/v1/admin/rhetoric/devices/" + id(device) + "/components").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"DEFINITION","heading":"التعريف","body":"تشبيه طرف بآخر"}
                """.formatted(version(device)))).andExpect(status().isOk()).andReturn());
        device = body(mockMvc.perform(post("/api/v1/admin/rhetoric/devices/" + id(device) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"CONSTRUCTED","text":"العلم نور","explanation":"مثال تحريري","alternativeInterpretation":"قد يقرأ مجازًا","scholarlyNote":"ليست القراءة الوحيدة"}
                """.formatted(version(device)))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/rhetoric/devices/" + id(device) + "/relations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"targetDeviceId":"%s","kind":"CONTRASTS_WITH"}
                """.formatted(version(device), id(device)))).andExpect(status().isConflict());
        device = body(mockMvc.perform(post("/api/v1/admin/rhetoric/devices/" + id(device) + "/relations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"targetDeviceId":"%s","kind":"OFTEN_CONFUSED_WITH"}
                """.formatted(version(device), id(other)))).andExpect(status().isOk()).andReturn());
        device = cite(staff.editor, "/api/v1/admin/rhetoric/devices/" + id(device) + "/citations", device, citation);
        device = workflow(staff, "/api/v1/admin/rhetoric/devices/" + id(device), device);
        mockMvc.perform(get("/api/v1/public/rhetoric/devices/" + slug(device)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.examples[0].alternativeInterpretation").value("قد يقرأ مجازًا"))
                .andExpect(jsonPath("$.data.relations[0].kind").value("OFTEN_CONFUSED_WITH"));
        mockMvc.perform(get("/api/v1/public/rhetoric/topics/" + slug(topic))).andExpect(status().isOk());
        Integer audits = jdbc.queryForObject("select count(*) from admin_audit_event where event_type = 'RHETORIC_DEVICE_PUBLISHED' and target_id = ?", Integer.class, id(device));
        assertThat(audits).isEqualTo(1);
    }

    @Test
    void literatureKeepsUncertainDatesAliasesAndRights() throws Exception {
        Staff staff = staff();
        mockMvc.perform(get("/api/v1/public/literature/eras")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/literature/figures").header("Authorization", bearer(staff.auditor))).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/literature/figures")).andExpect(status().isUnauthorized());
        int mark = SEQUENCE.incrementAndGet();
        String label = "غف" + arabicMark(mark);
        String citation = citation(staff);
        String era = catalog(staff, "/api/v1/admin/literature/eras", """
                {"name":"حقبة %s","summary":"حدود تقريبية","historicalContext":"سياق","displayOrder":1}
                """.formatted(label), citation);
        String genre = catalog(staff, "/api/v1/admin/literature/genres", """
                {"name":"شعر %s","description":"كلام موزون"}
                """.formatted(label), citation);
        String school = catalog(staff, "/api/v1/admin/literature/schools", """
                {"name":"مدرسة %s","description":"اتجاه أدبي","eraId":"%s"}
                """.formatted(label, id(era)), citation);
        mockMvc.perform(post("/api/v1/admin/literature/figures").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"canonicalName":"علم","biographySummary":"نبذة","birth":{"precision":"YEAR","year":0}}
                """)).andExpect(status().isBadRequest());
        String figure = body(mockMvc.perform(post("/api/v1/admin/literature/figures").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"canonicalName":"علم %s","biographySummary":"نبذة موثقة موجزة","birth":{"precision":"APPROXIMATE","calendar":"HIJRI","year":150,"circa":true},"death":{"precision":"UNKNOWN"}}
                """.formatted(label))).andExpect(status().isOk()).andReturn());
        figure = body(mockMvc.perform(post("/api/v1/admin/literature/figures/" + id(figure) + "/aliases").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"alias":"كنية %s","kind":"KUNYA"}
                """.formatted(version(figure), label))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/literature/figures/" + id(figure) + "/aliases").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"alias":"كنية %s","kind":"LAQAB"}
                """.formatted(version(figure), label))).andExpect(status().isConflict());
        figure = body(mockMvc.perform(post("/api/v1/admin/literature/figures/" + id(figure) + "/roles").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"role":"POET"}
                """.formatted(version(figure)))).andExpect(status().isOk()).andReturn());
        figure = body(mockMvc.perform(post("/api/v1/admin/literature/figures/" + id(figure) + "/roles").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"role":"WRITER"}
                """.formatted(version(figure)))).andExpect(status().isOk()).andReturn());
        figure = body(mockMvc.perform(post("/api/v1/admin/literature/figures/" + id(figure) + "/eras").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"eraId":"%s"}
                """.formatted(version(figure), id(era)))).andExpect(status().isOk()).andReturn());
        figure = body(mockMvc.perform(post("/api/v1/admin/literature/figures/" + id(figure) + "/schools").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"schoolId":"%s"}
                """.formatted(version(figure), id(school)))).andExpect(status().isOk()).andReturn());
        figure = cite(staff.editor, "/api/v1/admin/literature/figures/" + id(figure) + "/citations", figure, citation);
        figure = workflow(staff, "/api/v1/admin/literature/figures/" + id(figure), figure);
        mockMvc.perform(get("/api/v1/public/literature/figures/" + slug(figure)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.birthLabel").value("نحو سنة 150 هـ"))
                .andExpect(jsonPath("$.data.deathLabel").value("التاريخ غير معروف"))
                .andExpect(jsonPath("$.data.roles", org.hamcrest.Matchers.containsInAnyOrder("POET", "WRITER")))
                .andExpect(jsonPath("$.data.aliases[0].alias").value("كنية " + label));
        mockMvc.perform(get("/api/v1/public/search").param("q", "كنية " + label))
                .andExpect(jsonPath("$.data.items[?(@.id == '" + id(figure) + "')].matchReason").value(org.hamcrest.Matchers.hasItem("ALIAS")));

        String work = body(mockMvc.perform(post("/api/v1/admin/literature/works").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"عمل %s","description":"وصف بيانات لا نص كامل","languageCode":"ar","genreId":"%s","eraId":"%s"}
                """.formatted(label, id(genre), id(era)))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/literature/works/" + id(work) + "/excerpts").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"text":"مقتطف قصير","citationId":"%s"}
                """.formatted(version(work), id(citation)))).andExpect(status().isBadRequest());
        work = body(mockMvc.perform(patch("/api/v1/admin/literature/works/" + id(work) + "/rights").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"rights":"PUBLIC_DOMAIN","rightsNote":"قرار تحريري موثق"}
                """.formatted(version(work)))).andExpect(status().isOk()).andReturn());
        work = body(mockMvc.perform(post("/api/v1/admin/literature/works/" + id(work) + "/excerpts").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"text":"بيت قصير مسموح","citationId":"%s"}
                """.formatted(version(work), id(citation)))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(patch("/api/v1/admin/literature/works/" + id(work) + "/rights").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"rights":"RESTRICTED","rightsNote":"لا يجوز المقتطف"}
                """.formatted(version(work)))).andExpect(status().isConflict());
        work = body(mockMvc.perform(post("/api/v1/admin/literature/works/" + id(work) + "/figures").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"figureId":"%s","roleLabel":"شاعر"}
                """.formatted(version(work), id(figure)))).andExpect(status().isOk()).andReturn());
        work = cite(staff.editor, "/api/v1/admin/literature/works/" + id(work) + "/citations", work, citation);
        work = workflow(staff, "/api/v1/admin/literature/works/" + id(work), work);
        mockMvc.perform(get("/api/v1/public/literature/works/" + slug(work)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rights").value("PUBLIC_DOMAIN"))
                .andExpect(jsonPath("$.data.excerpts[0].text").value("بيت قصير مسموح"))
                .andExpect(jsonPath("$.data.genre.name").value("شعر " + label));
        mockMvc.perform(get("/api/v1/public/literature/eras/" + slug(era))).andExpect(status().isOk());
        Integer audits = jdbc.queryForObject("select count(*) from admin_audit_event where event_type in ('LITERARY_FIGURE_CREATED','LITERARY_WORK_PUBLISHED') and target_id in (?, ?)", Integer.class, id(figure), id(work));
        assertThat(audits).isGreaterThan(0);
    }

    @Test
    void articlesKeepSectionsCitationsAndPublishedRelations() throws Exception {
        Staff staff = staff();
        mockMvc.perform(get("/api/v1/public/articles")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/articles")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/admin/articles").header("Authorization", bearer(staff.auditor))).andExpect(status().isForbidden());
        int mark = SEQUENCE.incrementAndGet();
        String label = "قك" + arabicMark(mark);
        String citation = citation(staff);
        String topic = publishedTopic(staff, "مقال" + label, citation);
        String rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","title":"ربط %s","summary":"ملخص","coreRule":"قاعدة للربط","difficulty":"BEGINNER"}
                """.formatted(id(topic), label))).andExpect(status().isOk()).andReturn());
        String article = body(mockMvc.perform(post("/api/v1/admin/articles").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"مقالة %s","excerpt":"خلاصة معرفية","articleType":"LINGUISTIC","editorName":"التحرير"}
                """.formatted(label))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/articles/" + id(article) + "/relations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"targetType":"SPELLING_RULE","targetId":"%s"}
                """.formatted(version(article), id(rule)))).andExpect(status().isNotFound());
        rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/clauses").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"NOTE","heading":"ملاحظة","body":"ملاحظة للربط"}
                """.formatted(version(rule)))).andExpect(status().isOk()).andReturn());
        rule = cite(staff.editor, "/api/v1/admin/spelling/rules/" + id(rule) + "/citations", rule, citation);
        rule = workflow(staff, "/api/v1/admin/spelling/rules/" + id(rule), rule);
        article = body(mockMvc.perform(post("/api/v1/admin/articles/" + id(article) + "/sections").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"heading":"قسم","body":"متن يتحدث عن ربط %s دون أن يكون عنوان المقالة"}
                """.formatted(version(article), label))).andExpect(status().isOk()).andReturn());
        String sectionId = JsonPath.read(article, "$.data.sections[0].id");
        article = body(mockMvc.perform(post("/api/v1/admin/articles/" + id(article) + "/tags").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"name":"وسم %s"}
                """.formatted(version(article), label))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/articles/" + id(article) + "/tags").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"name":"وسم %s"}
                """.formatted(version(article), label))).andExpect(status().isConflict());
        article = body(mockMvc.perform(post("/api/v1/admin/articles/" + id(article) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s","sectionId":"%s"}
                """.formatted(version(article), id(citation), sectionId))).andExpect(status().isOk()).andReturn());
        article = body(mockMvc.perform(post("/api/v1/admin/articles/" + id(article) + "/relations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"targetType":"SPELLING_RULE","targetId":"%s"}
                """.formatted(version(article), id(rule)))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/articles/" + id(article) + "/relations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"targetType":"SPELLING_RULE","targetId":"%s"}
                """.formatted(version(article), id(rule)))).andExpect(status().isConflict());
        article = workflow(staff, "/api/v1/admin/articles/" + id(article), article);
        mockMvc.perform(get("/api/v1/public/articles/" + slug(article)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sections[0].heading").value("قسم"))
                .andExpect(jsonPath("$.data.tags[0]").value("وسم " + label))
                .andExpect(jsonPath("$.data.relations[0].url").value("/spelling/rules/" + slug(rule)));
        mockMvc.perform(get("/api/v1/public/search").param("q", "مقالة " + label).param("type", "articles"))
                .andExpect(jsonPath("$.data.items[0].type").value("ARTICLE"));
        mockMvc.perform(get("/api/v1/public/search/suggestions").param("q", "متن"))
                .andExpect(jsonPath("$.data[?(@.title == 'مقالة " + label + "')]").isEmpty());
        Integer audits = jdbc.queryForObject("select count(*) from admin_audit_event where event_type = 'ARTICLE_PUBLISHED' and target_id = ?", Integer.class, id(article));
        assertThat(audits).isEqualTo(1);
    }

    @Test
    void searchKeepsDictionaryExactAboveArticleBodyAndRepairsS6() throws Exception {
        Staff staff = staff();
        int mark = SEQUENCE.incrementAndGet();
        String label = "كت" + arabicMark(mark);
        String citation = citation(staff);
        String root = publishRoot(staff, "كتب");
        String entry = publishEntry(staff, "كتاب" + label, id(root), citation);
        String article = body(mockMvc.perform(post("/api/v1/admin/articles").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"حديث %s","excerpt":"خلاصة","articleType":"REFERENCE","editorName":"التحرير"}
                """.formatted(label))).andExpect(status().isOk()).andReturn());
        article = body(mockMvc.perform(post("/api/v1/admin/articles/" + id(article) + "/sections").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"heading":"المتن","body":"يذكر هذا القسم كلمة كتاب%s في الشرح فقط"}
                """.formatted(version(article), label))).andExpect(status().isOk()).andReturn());
        article = cite(staff.editor, "/api/v1/admin/articles/" + id(article) + "/citations", article, citation);
        article = workflow(staff, "/api/v1/admin/articles/" + id(article), article);
        mockMvc.perform(get("/api/v1/public/search").param("q", "كتاب"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/search").param("q", "كتاب" + label))
                .andExpect(jsonPath("$.data.items[0].type").value("DICTIONARY_ENTRY"))
                .andExpect(jsonPath("$.data.items[0].id").value(id(entry)));
        mockMvc.perform(post("/api/v1/admin/search/reindex").header("Authorization", bearer(staff.owner))).andExpect(status().isOk());
        jdbc.update("delete from search_document where entity_type = 'ARTICLE' and entity_id = ?::uuid", id(article));
        mockMvc.perform(get("/api/v1/admin/search/status").header("Authorization", bearer(staff.owner)))
                .andExpect(jsonPath("$.data.missingDocuments").value(org.hamcrest.Matchers.greaterThan(0)));
        mockMvc.perform(post("/api/v1/admin/search/repair").header("Authorization", bearer(staff.owner))).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/search").param("q", "حديث " + label).param("type", "content"))
                .andExpect(jsonPath("$.data.items[?(@.id == '" + id(article) + "')]").isNotEmpty());
    }

    @Test
    void concurrentEditsAndPublicationStayConsistent() throws Exception {
        Staff staff = staff();
        int mark = SEQUENCE.incrementAndGet();
        String label = "سب" + arabicMark(mark);
        String citation = citation(staff);
        String topic = publishedTopic(staff, label, citation);
        String rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","title":"سباق %s","summary":"ملخص","coreRule":"نص السباق","difficulty":"BEGINNER"}
                """.formatted(id(topic), label))).andExpect(status().isOk()).andReturn());
        long stale = version(rule);
        AtomicInteger updates = new AtomicInteger();
        AtomicInteger updateConflicts = new AtomicInteger();
        String ruleId = id(rule);
        race(2, () -> count(statusOf(patch("/api/v1/admin/spelling/rules/" + ruleId).header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"title":"سباق %s","summary":"ملخص معدل","coreRule":"نص","difficulty":"BEGINNER"}
                """.formatted(stale, label))), updates, updateConflicts));
        assertThat(updates.get()).isEqualTo(1);
        assertThat(updateConflicts.get()).isEqualTo(1);

        rule = body(mockMvc.perform(get("/api/v1/admin/spelling/rules/" + ruleId).header("Authorization", bearer(staff.editor))).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + ruleId + "/clauses").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"DEFINITION","heading":"تعريف","body":"تعريف السباق"}
                """.formatted(version(rule)))).andExpect(status().isOk()).andReturn());
        rule = cite(staff.editor, "/api/v1/admin/spelling/rules/" + ruleId + "/citations", rule, citation);
        rule = act(staff.editor, "/api/v1/admin/spelling/rules/" + ruleId + "/submit", rule);
        long reviewVersion = version(rule);
        AtomicInteger verifies = new AtomicInteger();
        AtomicInteger verifyConflicts = new AtomicInteger();
        race(2, () -> count(statusOf(post("/api/v1/admin/spelling/rules/" + ruleId + "/verify").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content(versionBody(reviewVersion))), verifies, verifyConflicts));
        race(2, () -> count(statusOf(post("/api/v1/admin/spelling/rules/" + ruleId + "/verify").header("Authorization", bearer(staff.reviewerTwo)).contentType(MediaType.APPLICATION_JSON).content(versionBody(reviewVersion))), verifies, verifyConflicts));
        assertThat(verifies.get()).isEqualTo(1);
        assertThat(verifyConflicts.get()).isGreaterThanOrEqualTo(1);

        rule = body(mockMvc.perform(get("/api/v1/admin/spelling/rules/" + ruleId).header("Authorization", bearer(staff.publisher))).andReturn());
        long publishVersion = version(rule);
        AtomicInteger published = new AtomicInteger();
        AtomicInteger publishConflicts = new AtomicInteger();
        race(2, () -> {
            count(statusOf(post("/api/v1/admin/spelling/rules/" + ruleId + "/publish").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(publishVersion))), published, publishConflicts);
            count(statusOf(patch("/api/v1/admin/spelling/rules/" + ruleId).header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                    {"version":%d,"title":"أثناء النشر","summary":"ملخص","coreRule":"نص","difficulty":"BEGINNER"}
                    """.formatted(publishVersion))), published, publishConflicts);
        });
        assertThat(published.get()).isGreaterThanOrEqualTo(1);

        String article = body(mockMvc.perform(post("/api/v1/admin/articles").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"أرشيف %s","excerpt":"خلاصة","articleType":"EDITORIAL","editorName":"التحرير"}
                """.formatted(label))).andExpect(status().isOk()).andReturn());
        article = body(mockMvc.perform(post("/api/v1/admin/articles/" + id(article) + "/sections").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"heading":"قسم","body":"متن الأرشيف"}
                """.formatted(version(article)))).andExpect(status().isOk()).andReturn());
        article = cite(staff.editor, "/api/v1/admin/articles/" + id(article) + "/citations", article, citation);
        article = act(staff.editor, "/api/v1/admin/articles/" + id(article) + "/submit", article);
        article = act(staff.reviewer, "/api/v1/admin/articles/" + id(article) + "/verify", article);
        long articleVersion = version(article);
        String articleId = id(article);
        AtomicInteger articleResults = new AtomicInteger();
        race(2, () -> {
            int publishStatus = statusOf(post("/api/v1/admin/articles/" + articleId + "/publish").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(articleVersion)));
            int reindexStatus = statusOf(post("/api/v1/admin/search/reindex").header("Authorization", bearer(staff.owner)));
            if ((publishStatus == 200 || publishStatus == 409) && (reindexStatus == 200 || reindexStatus == 409)) {
                articleResults.incrementAndGet();
            } else {
                throw new AssertionError(publishStatus + "/" + reindexStatus);
            }
        });
        assertThat(articleResults.get()).isEqualTo(2);
        article = body(mockMvc.perform(get("/api/v1/admin/articles/" + articleId).header("Authorization", bearer(staff.publisher))).andReturn());
        if (!"PUBLISHED".equals(JsonPath.read(article, "$.data.status"))) {
            article = act(staff.publisher, "/api/v1/admin/articles/" + articleId + "/publish", article);
        }
        long archiveVersion = version(article);
        race(2, () -> {
            statusOf(post("/api/v1/admin/articles/" + articleId + "/archive").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(archiveVersion)));
            statusOf(get("/api/v1/public/search").param("q", "أرشيف " + label));
        });
        mockMvc.perform(get("/api/v1/public/search").param("q", "أرشيف " + label))
                .andExpect(jsonPath("$.data.items[?(@.id == '" + articleId + "')]").isEmpty());
        mockMvc.perform(get("/api/v1/public/articles/" + slug(article))).andExpect(status().isNotFound());
    }

    private String publishedTopic(Staff staff, String label, String citation) throws Exception {
        String topic = body(mockMvc.perform(post("/api/v1/admin/spelling/topics").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"موضوع %s","summary":"ملخص","displayOrder":1}
                """.formatted(label))).andExpect(status().isOk()).andReturn());
        topic = cite(staff.editor, "/api/v1/admin/spelling/topics/" + id(topic) + "/citations", topic, citation);
        return workflow(staff, "/api/v1/admin/spelling/topics/" + id(topic), topic);
    }

    private String publishedDevice(Staff staff, String topicId, String name, String citation) throws Exception {
        String device = body(mockMvc.perform(post("/api/v1/admin/rhetoric/devices").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","name":"%s","shortDefinition":"تعريف موجز","detailedExplanation":"شرح"}
                """.formatted(topicId, name))).andExpect(status().isOk()).andReturn());
        device = body(mockMvc.perform(post("/api/v1/admin/rhetoric/devices/" + id(device) + "/components").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"NOTE","heading":"ملاحظة","body":"ملاحظة بلاغية"}
                """.formatted(version(device)))).andExpect(status().isOk()).andReturn());
        device = cite(staff.editor, "/api/v1/admin/rhetoric/devices/" + id(device) + "/citations", device, citation);
        return workflow(staff, "/api/v1/admin/rhetoric/devices/" + id(device), device);
    }

    private String catalog(Staff staff, String path, String json, String citation) throws Exception {
        String record = body(mockMvc.perform(post(path).header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk()).andReturn());
        record = cite(staff.editor, path + "/" + id(record) + "/citations", record, citation);
        return workflow(staff, path + "/" + id(record), record);
    }

    private String workflow(Staff staff, String path, String record) throws Exception {
        record = act(staff.editor, path + "/submit", record);
        record = act(staff.reviewer, path + "/verify", record);
        return act(staff.publisher, path + "/publish", record);
    }

    private String cite(String token, String path, String record, String citation) throws Exception {
        return body(mockMvc.perform(post(path).header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(record), id(citation)))).andExpect(status().isOk()).andReturn());
    }

    private String citation(Staff staff) throws Exception {
        String source = body(mockMvc.perform(post("/api/v1/admin/sources").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"sourceType":"DICTIONARY","title":"مقاييس اللغة","author":"ابن فارس","edition":"اختبار","licenseType":"PUBLIC_DOMAIN","publicDomain":true,"attributionText":"اختبار محلي"}
                """)).andExpect(status().isOk()).andReturn());
        source = act(staff.editor, "/api/v1/admin/sources/" + id(source) + "/submit", source);
        source = act(staff.reviewer, "/api/v1/admin/sources/" + id(source) + "/verify", source);
        act(staff.publisher, "/api/v1/admin/sources/" + id(source) + "/publish", source);
        return body(mockMvc.perform(post("/api/v1/admin/sources/" + id(source) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"pageFrom":1,"pageTo":2,"entryLabel":"اختبار"}
                """)).andExpect(status().isOk()).andReturn());
    }

    private String publishRoot(Staff staff, String original) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/admin/dictionary/roots").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"original":"%s"}
                """.formatted(original))).andReturn();
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

    private String publishEntry(Staff staff, String lemma, String rootId, String citation) throws Exception {
        String entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"lemma":"%s","rootId":"%s","partOfSpeech":"NOUN","gender":"MASCULINE"}
                """.formatted(lemma, rootId))).andExpect(status().isOk()).andReturn());
        entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/senses").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"sense":{"definition":"ما يكتب فيه","shortDefinition":"كتاب","displayOrder":1}}
                """.formatted(version(entry)))).andExpect(status().isOk()).andReturn());
        entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/senses/" + JsonPath.read(entry, "$.data.senses[0].id") + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(entry), id(citation)))).andExpect(status().isOk()).andReturn());
        entry = act(staff.editor, "/api/v1/admin/dictionary/entries/" + id(entry) + "/submit", entry);
        entry = act(staff.reviewer, "/api/v1/admin/dictionary/entries/" + id(entry) + "/verify", entry);
        return act(staff.publisher, "/api/v1/admin/dictionary/entries/" + id(entry) + "/publish", entry);
    }

    private Staff staff() throws Exception {
        Tokens owner = login("owner", "Owner-Pass-123!");
        int sequence = SEQUENCE.incrementAndGet();
        return new Staff(
                owner.accessToken(),
                member(owner, "s6e" + sequence, EDITOR_ROLE),
                member(owner, "s6r" + sequence, REVIEWER_ROLE),
                member(owner, "s6t" + sequence, REVIEWER_ROLE),
                member(owner, "s6p" + sequence, PUBLISHER_ROLE),
                member(owner, "s6a" + sequence, AUDITOR_ROLE));
    }

    private String member(Tokens owner, String username, UUID role) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(owner.accessToken())).contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"%s","email":"%s@arabic-reference.test","displayName":"%s","roleIds":["%s"]}
                """.formatted(username, username, username, role))).andExpect(status().isOk()).andReturn();
        String temporary = JsonPath.read(created.getResponse().getContentAsString(), "$.data.temporaryPassword");
        return changePassword(login(username, temporary), temporary, "Editor-Pass-123!").accessToken();
    }

    private String act(String token, String path, String resource) throws Exception {
        return body(mockMvc.perform(post(path).header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(resource)))).andExpect(status().isOk()).andReturn());
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

    private static String slug(String body) {
        return JsonPath.read(body, "$.data.slug");
    }

    private static long version(String body) {
        return ((Number) JsonPath.read(body, "$.data.version")).longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String arabicMark(int mark) {
        String letters = "ابتثجحخدذرزسشصضطظعغفقكلمنهوي";
        StringBuilder builder = new StringBuilder();
        int value = Math.max(mark, 1);
        do {
            builder.append(letters.charAt(value % letters.length()));
            value /= letters.length();
        } while (value > 0);
        return builder.toString();
    }

    private record Tokens(String accessToken, String refreshToken) {
    }

    private record Staff(String owner, String editor, String reviewer, String reviewerTwo, String publisher, String auditor) {
    }
}
