package com.mrsoft.arabicreference.grammar;

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
class GrammarIntegrationTest {

    private static final UUID EDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000003");
    private static final UUID REVIEWER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000004");
    private static final UUID PUBLISHER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000005");
    private static final UUID AUDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    private static final String FA3ALA = "b0000000-0000-4000-8000-000000000001";
    private static final AtomicInteger SEQUENCE = new AtomicInteger(80);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.rate-limit.login", () -> "1000");
        registry.add("app.admin.rate-limit.refresh", () -> "1000");
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
    void grammarKnowledgeIsStructuredReviewedAndPublicOnlyWhenPublished() throws Exception {
        Integer version5 = jdbc.queryForObject("select count(*) from flyway_schema_history where success = true and version = '5'", Integer.class);
        assertThat(version5).isEqualTo(1);
        mockMvc.perform(get("/api/v1/public/grammar/topics")).andExpect(status().isOk()).andExpect(jsonPath("$.data.title").value("النحو"));
        mockMvc.perform(get("/api/v1/public/grammar/search").param("q", " ")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/grammar/search").param("q", "book")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/admin/grammar/topics")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/public/grammar/topics").param("size", "51")).andExpect(status().isBadRequest());

        Staff staff = staff();
        mockMvc.perform(get("/api/v1/admin/grammar/topics").header("Authorization", bearer(staff.auditor))).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/grammar/rules").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());

        int mark = SEQUENCE.incrementAndGet();
        String parent = createTopic(staff.editor, "المرفوعات" + mark, "باب المرفوعات في هذا الاختبار", "MARFUAT", null);
        String child = createTopic(staff.editor, "الفاعل" + mark, "تعريف الفاعل في هذا الاختبار", "MARFUAT", id(parent));
        String grandchild = createTopic(staff.editor, "فاعل ظاهر" + mark, "فرع للاختبار", "MARFUAT", id(child));
        mockMvc.perform(post("/api/v1/admin/grammar/topics/" + id(child) + "/parent").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(parentBody(version(child), id(child)))).andExpect(status().isConflict());
        mockMvc.perform(post("/api/v1/admin/grammar/topics/" + id(parent) + "/parent").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(parentBody(version(parent), id(grandchild)))).andExpect(status().isConflict());
        mockMvc.perform(get("/api/v1/public/grammar/topics/" + slug(child))).andExpect(status().isNotFound());

        String citation = citation(staff, "PUBLIC_DOMAIN", true);
        parent = act(staff.editor, "/api/v1/admin/grammar/topics/" + id(parent) + "/submit", parent);
        parent = act(staff.reviewer, "/api/v1/admin/grammar/topics/" + id(parent) + "/verify", parent);
        mockMvc.perform(post("/api/v1/admin/grammar/topics/" + id(parent) + "/publish").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(parent)))).andExpect(status().isForbidden());
        parent = act(staff.publisher, "/api/v1/admin/grammar/topics/" + id(parent) + "/publish", parent);
        child = body(mockMvc.perform(post("/api/v1/admin/grammar/topics/" + id(child) + "/prerequisites").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"requiredTopicId":"%s"}
                """.formatted(version(child), id(parent)))).andExpect(status().isOk()).andReturn());
        child = act(staff.editor, "/api/v1/admin/grammar/topics/" + id(child) + "/submit", child);
        child = act(staff.reviewer, "/api/v1/admin/grammar/topics/" + id(child) + "/verify", child);
        child = act(staff.publisher, "/api/v1/admin/grammar/topics/" + id(child) + "/publish", child);
        mockMvc.perform(get("/api/v1/public/grammar/topics/" + slug(child)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prerequisites[0].slug").value(slug(parent)))
                .andExpect(jsonPath("$.data.createdBy").doesNotExist());

        String rule = createRule(staff.editor, id(child), "قاعدة الفاعل" + mark, "يرفع الفاعل");
        rule = addComponent(staff.editor, rule, "DEFINITION", "الفاعل اسم مرفوع");
        rule = addComponent(staff.editor, rule, "CONDITION", "يتقدم عليه فعل");
        rule = addComponent(staff.editor, rule, "EXCEPTION", "قد يحذف إذا دل عليه دليل");
        String componentId = JsonPath.read(rule, "$.data.components[2].id");
        rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/components/" + componentId + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(rule), id(citation)))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"textOriginal":"قال الشاعر","exampleType":"QUOTED"}
                """.formatted(version(rule)))).andExpect(status().isBadRequest());
        rule = addExample(staff.editor, rule, "CONSTRUCTED", "كتب الطالب الدرس", null);
        rule = addExample(staff.editor, rule, "COUNTEREXAMPLE", "الطالب كتب الدرس", null);
        rule = addExample(staff.editor, rule, "QUOTED", "جاء زيد", id(citation));
        rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(rule), id(citation)))).andExpect(status().isOk()).andReturn());

        String other = createRule(staff.editor, id(child), "نائب الفاعل" + mark, "يحل محل الفاعل");
        other = addComponent(staff.editor, other, "CORE_RULE", "نائب الفاعل مرفوع");
        other = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(other) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(other), id(citation)))).andExpect(status().isOk()).andReturn());
        other = publishRule(staff, other);
        rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/relations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"targetRuleId":"%s","type":"CONTRASTS_WITH"}
                """.formatted(version(rule), id(other)))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/relations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"targetRuleId":"%s","type":"CONTRASTS_WITH"}
                """.formatted(version(rule), id(other)))).andExpect(status().isConflict());

        String concept = createConcept(staff.editor, "الإعراب" + mark, "تغير أواخر الكلم", "الإعراب أثر يجلبه العامل");
        concept = body(mockMvc.perform(post("/api/v1/admin/grammar/concepts/" + id(concept) + "/aliases").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"alias":"علم الإعراب%d"}
                """.formatted(version(concept), mark))).andExpect(status().isOk()).andReturn());
        mockMvc.perform(post("/api/v1/admin/grammar/concepts/" + id(concept) + "/aliases").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"alias":"علم الإعراب%d"}
                """.formatted(version(concept), mark))).andExpect(status().isConflict());
        concept = body(mockMvc.perform(post("/api/v1/admin/grammar/concepts/" + id(concept) + "/rules").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"ruleId":"%s"}
                """.formatted(version(concept), id(rule)))).andExpect(status().isOk()).andReturn());
        concept = body(mockMvc.perform(post("/api/v1/admin/grammar/concepts/" + id(concept) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(concept), id(citation)))).andExpect(status().isOk()).andReturn());
        concept = publishConcept(staff, concept);

        mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/verify").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(rule)))).andExpect(status().isForbidden());
        rule = act(staff.editor, "/api/v1/admin/grammar/rules/" + id(rule) + "/submit", rule);
        rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/request-changes").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"reason":"وضّح الشرط"}
                """.formatted(version(rule)))).andExpect(status().isOk()).andReturn());
        rule = act(staff.editor, "/api/v1/admin/grammar/rules/" + id(rule) + "/submit", rule);
        mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/verify").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(rule)))).andExpect(status().isForbidden());
        rule = act(staff.reviewer, "/api/v1/admin/grammar/rules/" + id(rule) + "/verify", rule);
        mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/publish").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(rule)))).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/publish").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(rule)))).andExpect(status().isForbidden());
        String publishedTitle = JsonPath.read(rule, "$.data.title");
        rule = act(staff.publisher, "/api/v1/admin/grammar/rules/" + id(rule) + "/publish", rule);
        mockMvc.perform(get("/api/v1/public/grammar/rules/" + slug(rule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.components[?(@.type == 'CONDITION')]").isNotEmpty())
                .andExpect(jsonPath("$.data.components[?(@.type == 'EXCEPTION')].sources[0].title").isNotEmpty())
                .andExpect(jsonPath("$.data.examples[?(@.exampleType == 'COUNTEREXAMPLE')].editorialNote").isNotEmpty())
                .andExpect(jsonPath("$.data.relations[0].slug").value(slug(other)))
                .andExpect(jsonPath("$.data.createdBy").doesNotExist());
        mockMvc.perform(get("/api/v1/public/grammar/concepts/" + slug(concept)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aliases[0]").value("علم الإعراب" + mark))
                .andExpect(jsonPath("$.data.sources[0].title").exists());
        mockMvc.perform(get("/api/v1/public/grammar/search").param("q", "علم الإعراب" + mark))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[?(@.slug == '" + slug(concept) + "')]").isNotEmpty());

        String revised = body(mockMvc.perform(patch("/api/v1/admin/grammar/rules/" + id(rule)).header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"title":"مسودة الفاعل%d","summary":"تعديل غير منشور","ruleText":"نص مسودة","difficulty":"BEGINNER","displayOrder":1}
                """.formatted(version(rule), mark))).andExpect(status().isOk()).andReturn());
        Integer revisions = jdbc.queryForObject("select count(*) from content_revision where target_type = 'grammar_rule' and target_id = ?::uuid", Integer.class, id(rule));
        assertThat(revisions).isGreaterThan(0);
        mockMvc.perform(get("/api/v1/public/grammar/rules/" + slug(rule))).andExpect(status().isOk()).andExpect(jsonPath("$.data.title").value(publishedTitle));
        revised = publishRule(staff, revised);
        mockMvc.perform(get("/api/v1/public/grammar/rules/" + slug(revised))).andExpect(status().isOk()).andExpect(jsonPath("$.data.title").value("مسودة الفاعل" + mark));
        String archived = act(staff.publisher, "/api/v1/admin/grammar/rules/" + id(revised) + "/archive", revised);
        mockMvc.perform(get("/api/v1/public/grammar/rules/" + slug(archived))).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/public/grammar/search").param("q", "مسودة الفاعل" + mark)).andExpect(status().isOk()).andExpect(jsonPath("$.data.items[?(@.slug == '" + slug(archived) + "')]").isEmpty());

        String restricted = citation(staff, "RESTRICTED", false);
        String blocked = createRule(staff.editor, id(child), "قاعدة مقيدة" + mark, "لا تنشر");
        blocked = addComponent(staff.editor, blocked, "CORE_RULE", "نص لا يصلح للنشر");
        blocked = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(blocked) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(blocked), id(restricted)))).andExpect(status().isOk()).andReturn());
        blocked = act(staff.editor, "/api/v1/admin/grammar/rules/" + id(blocked) + "/submit", blocked);
        blocked = act(staff.reviewer, "/api/v1/admin/grammar/rules/" + id(blocked) + "/verify", blocked);
        mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(blocked) + "/publish").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version(blocked)))).andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/grammar/annotations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("{\"sentence\":\" \"}")).andExpect(status().isBadRequest());
        String annotation = body(mockMvc.perform(post("/api/v1/admin/grammar/annotations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"sentence":"كتب الطالب الدرس"}
                """)).andExpect(status().isOk()).andReturn());
        mockMvc.perform(get("/api/v1/public/grammar/annotations/" + id(annotation))).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/admin/grammar/annotations/" + id(annotation) + "/tokens").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"tokens":[{"surface":"كتب","position":0},{"surface":"كتب","position":0}]}
                """.formatted(version(annotation)))).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/grammar/annotations/" + id(annotation) + "/tokens").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"tokens":[{"surface":"كتب","position":0,"roleCode":"UNKNOWN","grammaticalState":"RAFA"}]}
                """.formatted(version(annotation)))).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/grammar/annotations/" + id(annotation) + "/tokens").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"tokens":[{"surface":"الطالب","position":0,"roleCode":"FAIL","grammaticalState":"JAZM"}]}
                """.formatted(version(annotation)))).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/grammar/annotations/" + id(annotation) + "/tokens").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"tokens":[{"surface":"كتب","position":0,"roleCode":"FI3L","grammaticalState":"JARR"}]}
                """.formatted(version(annotation)))).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/grammar/annotations/" + id(annotation) + "/tokens").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":99,"tokens":[{"surface":"كتب","position":0,"roleCode":"FI3L","grammaticalState":"RAFA"}]}
                """)).andExpect(status().isConflict());
        annotation = body(mockMvc.perform(post("/api/v1/admin/grammar/annotations/" + id(annotation) + "/tokens").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"tokens":[{"surface":"الدرس","position":2,"roleCode":"MAFUL_BIHI","grammaticalState":"NASB"},{"surface":"كتب","position":0,"roleCode":"FI3L","grammaticalState":"RAFA"},{"surface":"الطالب","position":1,"roleCode":"FAIL","grammaticalState":"RAFA"}]}
                """.formatted(version(annotation)))).andExpect(status().isOk()).andReturn());
        annotation = act(staff.editor, "/api/v1/admin/grammar/annotations/" + id(annotation) + "/submit", annotation);
        annotation = act(staff.reviewer, "/api/v1/admin/grammar/annotations/" + id(annotation) + "/verify", annotation);
        annotation = act(staff.publisher, "/api/v1/admin/grammar/annotations/" + id(annotation) + "/publish", annotation);
        mockMvc.perform(get("/api/v1/public/grammar/annotations/" + id(annotation)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokens[0].surface").value("كتب"))
                .andExpect(jsonPath("$.data.tokens[1].roleLabel").value("فاعل"))
                .andExpect(jsonPath("$.data.tokens[2].stateLabel").value("منصوب"));
    }

    @Test
    void grammarCrossLinksUsePublishedDictionaryAndMorphologyOnly() throws Exception {
        Staff staff = staff();
        int mark = SEQUENCE.incrementAndGet();
        String citation = citation(staff, "PUBLIC_DOMAIN", true);
        String root = publishRoot(staff, "كتب");
        String entry = publishEntry(staff, "كتاب" + arabicMark(mark), id(root), citation);
        String analysis = createAnalysis(staff.editor, id(entry));
        analysis = act(staff.editor, "/api/v1/admin/morphology/analyses/" + id(analysis) + "/submit", analysis);
        analysis = act(staff.reviewer, "/api/v1/admin/morphology/analyses/" + id(analysis) + "/verify", analysis);
        analysis = act(staff.publisher, "/api/v1/admin/morphology/analyses/" + id(analysis) + "/publish", analysis);

        String topic = createTopic(staff.editor, "روابط" + mark, "موضوع للربط", "OTHER", null);
        topic = act(staff.editor, "/api/v1/admin/grammar/topics/" + id(topic) + "/submit", topic);
        topic = act(staff.reviewer, "/api/v1/admin/grammar/topics/" + id(topic) + "/verify", topic);
        topic = act(staff.publisher, "/api/v1/admin/grammar/topics/" + id(topic) + "/publish", topic);
        String annotation = body(mockMvc.perform(post("/api/v1/admin/grammar/annotations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"sentence":"كتاب واضح"}
                """)).andExpect(status().isOk()).andReturn());
        annotation = body(mockMvc.perform(post("/api/v1/admin/grammar/annotations/" + id(annotation) + "/tokens").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"tokens":[{"surface":"كتاب","position":0,"roleCode":"MUBTADA","grammaticalState":"RAFA","lexicalEntryId":"%s","morphologyAnalysisId":"%s"}]}
                """.formatted(version(annotation), id(entry), id(analysis)))).andExpect(status().isOk()).andReturn());
        annotation = act(staff.editor, "/api/v1/admin/grammar/annotations/" + id(annotation) + "/submit", annotation);
        annotation = act(staff.reviewer, "/api/v1/admin/grammar/annotations/" + id(annotation) + "/verify", annotation);
        annotation = act(staff.publisher, "/api/v1/admin/grammar/annotations/" + id(annotation) + "/publish", annotation);
        String rule = createRule(staff.editor, id(topic), "ربط" + mark, "مثال مربوط");
        rule = addComponent(staff.editor, rule, "CORE_RULE", "الكلمة المربوطة تبقى مرجعًا");
        rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(rule), id(citation)))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"textOriginal":"كتاب واضح","exampleType":"CONSTRUCTED","annotationId":"%s"}
                """.formatted(version(rule), id(annotation)))).andExpect(status().isOk()).andReturn());
        rule = publishRule(staff, rule);
        mockMvc.perform(get("/api/v1/public/grammar/rules/" + slug(rule)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.examples[0].tokens[0].lexical.slug").value(slug(entry)))
                .andExpect(jsonPath("$.data.examples[0].tokens[0].morphology.patternOriginal").value("فَعَلَ"))
                .andExpect(jsonPath("$.data.examples[0].tokens[0].morphology.label").value("عرض التحليل الصرفي"));
    }

    @Test
    void concurrentGrammarEditsDoNotCorruptTheRecord() throws Exception {
        Staff staff = staff();
        int mark = SEQUENCE.incrementAndGet();
        String parent = createTopic(staff.editor, "أصل" + mark, "أصل الاختبار", "FOUNDATIONS", null);
        String left = createTopic(staff.editor, "فرع أ" + mark, "فرع", "FOUNDATIONS", null);
        String right = createTopic(staff.editor, "فرع ب" + mark, "فرع", "FOUNDATIONS", null);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        long leftVersion = version(left);
        long rightVersion = version(right);
        race(2, () -> {
            int status = statusOf(post("/api/v1/admin/grammar/topics/" + id(left) + "/parent").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(parentBody(leftVersion, id(right))));
            if (status == 200 || status == 409) {
                int other = statusOf(post("/api/v1/admin/grammar/topics/" + id(right) + "/parent").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(parentBody(rightVersion, id(left))));
                count(status, success, conflict);
                count(other, success, conflict);
            } else {
                throw new AssertionError("Unexpected status " + status);
            }
        });
        Integer cycles = jdbc.queryForObject("""
                with recursive walk as (
                    select id, parent_id, 1 as depth from grammar_topic where id = ?::uuid
                    union all
                    select topic.id, topic.parent_id, walk.depth + 1 from grammar_topic topic join walk on topic.id = walk.parent_id where walk.depth < 8
                )
                select count(*) from walk where parent_id = ?::uuid
                """, Integer.class, id(left), id(left));
        assertThat(cycles).isZero();

        String citation = citation(staff, "PUBLIC_DOMAIN", true);
        String rule = createRule(staff.editor, id(parent), "سباق" + mark, "نص");
        rule = addComponent(staff.editor, rule, "CORE_RULE", "قاعدة للسباق");
        rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(rule), id(citation)))).andExpect(status().isOk()).andReturn());
        long ruleVersion = version(rule);
        AtomicInteger updates = new AtomicInteger();
        AtomicInteger updateConflicts = new AtomicInteger();
        String ruleId = id(rule);
        race(2, () -> count(statusOf(patch("/api/v1/admin/grammar/rules/" + ruleId).header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"title":"سباق%d","summary":"ملخص","ruleText":"نص","displayOrder":1}
                """.formatted(ruleVersion, mark))), updates, updateConflicts));
        assertThat(updates.get()).isEqualTo(1);
        assertThat(updateConflicts.get()).isEqualTo(1);

        rule = body(mockMvc.perform(get("/api/v1/admin/grammar/rules/" + ruleId).header("Authorization", bearer(staff.editor))).andExpect(status().isOk()).andReturn());
        rule = act(staff.editor, "/api/v1/admin/grammar/rules/" + ruleId + "/submit", rule);
        long reviewVersion = version(rule);
        AtomicInteger verifies = new AtomicInteger();
        AtomicInteger verifyConflicts = new AtomicInteger();
        race(2, () -> count(statusOf(post("/api/v1/admin/grammar/rules/" + ruleId + "/verify").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content(versionBody(reviewVersion))), verifies, verifyConflicts));
        assertThat(verifies.get()).isEqualTo(1);
        assertThat(verifyConflicts.get()).isEqualTo(1);

        rule = body(mockMvc.perform(get("/api/v1/admin/grammar/rules/" + ruleId).header("Authorization", bearer(staff.publisher))).andExpect(status().isOk()).andReturn());
        long verifiedVersion = version(rule);
        AtomicInteger publishOk = new AtomicInteger();
        AtomicInteger editRejected = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Integer> publishFuture = executor.submit(() -> {
                start.await();
                return statusOf(post("/api/v1/admin/grammar/rules/" + ruleId + "/publish").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(verifiedVersion)));
            });
            Future<Integer> editFuture = executor.submit(() -> {
                start.await();
                return statusOf(patch("/api/v1/admin/grammar/rules/" + ruleId).header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                        {"version":%d,"title":"أثناء النشر","summary":"لا","ruleText":"لا","displayOrder":1}
                        """.formatted(verifiedVersion)));
            });
            start.countDown();
            if (publishFuture.get(30, TimeUnit.SECONDS) == 200) {
                publishOk.incrementAndGet();
            }
            if (editFuture.get(30, TimeUnit.SECONDS) == 409) {
                editRejected.incrementAndGet();
            }
        } finally {
            executor.shutdownNow();
        }
        assertThat(publishOk.get()).isEqualTo(1);
        assertThat(editRejected.get()).isEqualTo(1);
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

    private String createTopic(String token, String title, String summary, String category, String parentId) throws Exception {
        String parent = parentId == null ? "null" : "\"" + parentId + "\"";
        return body(mockMvc.perform(post("/api/v1/admin/grammar/topics").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"%s","summary":"%s","category":"%s","parentId":%s,"displayOrder":1}
                """.formatted(title, summary, category, parent))).andExpect(status().isOk()).andReturn());
    }

    private String createRule(String token, String topicId, String title, String ruleText) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/grammar/rules").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","title":"%s","summary":"ملخص تحريري","ruleText":"%s","displayOrder":1}
                """.formatted(topicId, title, ruleText))).andExpect(status().isOk()).andReturn());
    }

    private String addComponent(String token, String rule, String type, String bodyText) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/components").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"type":"%s","body":"%s","displayOrder":1}
                """.formatted(version(rule), type, bodyText))).andExpect(status().isOk()).andReturn());
    }

    private String addExample(String token, String rule, String type, String text, String citationId) throws Exception {
        String citation = citationId == null ? "null" : "\"" + citationId + "\"";
        return body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/examples").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"textOriginal":"%s","exampleType":"%s","citationId":%s}
                """.formatted(version(rule), text, type, citation))).andExpect(status().isOk()).andReturn());
    }

    private String createConcept(String token, String term, String shortDefinition, String detailed) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/grammar/concepts").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"term":"%s","shortDefinition":"%s","detailedDefinition":"%s"}
                """.formatted(term, shortDefinition, detailed))).andExpect(status().isOk()).andReturn());
    }

    private String publishRule(Staff staff, String rule) throws Exception {
        rule = act(staff.editor, "/api/v1/admin/grammar/rules/" + id(rule) + "/submit", rule);
        rule = act(staff.reviewer, "/api/v1/admin/grammar/rules/" + id(rule) + "/verify", rule);
        return act(staff.publisher, "/api/v1/admin/grammar/rules/" + id(rule) + "/publish", rule);
    }

    private String publishConcept(Staff staff, String concept) throws Exception {
        concept = act(staff.editor, "/api/v1/admin/grammar/concepts/" + id(concept) + "/submit", concept);
        concept = act(staff.reviewer, "/api/v1/admin/grammar/concepts/" + id(concept) + "/verify", concept);
        return act(staff.publisher, "/api/v1/admin/grammar/concepts/" + id(concept) + "/publish", concept);
    }

    private String citation(Staff staff, String license, boolean publish) throws Exception {
        String source = body(mockMvc.perform(post("/api/v1/admin/sources").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"sourceType":"DICTIONARY","title":"مقاييس اللغة","author":"ابن فارس","edition":"اختبار","licenseType":"%s","publicDomain":%s,"attributionText":"اختبار"}
                """.formatted(license, publish))).andExpect(status().isOk()).andReturn());
        if (publish) {
            source = act(staff.editor, "/api/v1/admin/sources/" + id(source) + "/submit", source);
            source = act(staff.reviewer, "/api/v1/admin/sources/" + id(source) + "/verify", source);
            act(staff.publisher, "/api/v1/admin/sources/" + id(source) + "/publish", source);
        }
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

    private String createAnalysis(String token, String entryId) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/morphology/analyses").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"lexicalEntryId":"%s","patternId":"%s","derivation":"ROOT_DERIVED","verbClass":"SOUND","imperfectVowel":"DAMMA","notes":"تحليل اختبار النحو"}
                """.formatted(entryId, FA3ALA))).andExpect(status().isOk()).andReturn());
    }

    private Staff staff() throws Exception {
        Tokens owner = login("owner", "Owner-Pass-123!");
        int sequence = SEQUENCE.incrementAndGet();
        return new Staff(owner.accessToken(), member(owner, "s4e" + sequence, EDITOR_ROLE, "Editor-Pass-123!"), member(owner, "s4r" + sequence, REVIEWER_ROLE, "Review-Pass-123!"), member(owner, "s4p" + sequence, PUBLISHER_ROLE, "Publish-Pass-123!"), member(owner, "s4a" + sequence, AUDITOR_ROLE, "Audit-Pass-123!"));
    }

    private String member(Tokens owner, String username, UUID role, String password) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(owner.accessToken())).contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"%s","email":"%s@arabic-reference.test","displayName":"%s","roleIds":["%s"]}
                """.formatted(username, username, username, role))).andExpect(status().isOk()).andReturn();
        String temporary = JsonPath.read(created.getResponse().getContentAsString(), "$.data.temporaryPassword");
        return changePassword(login(username, temporary), temporary, password).accessToken();
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

    private static String parentBody(long version, String parentId) {
        return "{\"version\":" + version + ",\"parentId\":\"" + parentId + "\"}";
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

    private record Tokens(String accessToken, String refreshToken) {
    }

    private record Staff(String owner, String editor, String reviewer, String publisher, String auditor) {
    }
}
