package com.mrsoft.arabicreference.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mrsoft.arabicreference.IntegrationContainers;
import java.util.List;
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
class ToolsIntegrationTest {

    private static final UUID EDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000003");
    private static final UUID REVIEWER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000004");
    private static final UUID PUBLISHER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000005");
    private static final UUID AUDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    private static final String FA3ALA = "b0000000-0000-4000-8000-000000000001";
    private static final AtomicInteger SEQUENCE = new AtomicInteger(400);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.rate-limit.login", () -> "1000");
        registry.add("app.admin.rate-limit.refresh", () -> "1000");
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> "Owner-Pass-123!");
        registry.add("spring.jpa.properties.hibernate.generate_statistics", () -> "true");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void anonymousToolsStayOpenAndAdminToolsStayClosed() throws Exception {
        assertThat(jdbc.queryForObject("select count(*) from flyway_schema_history where success = true and version = '12'", Integer.class)).isEqualTo(1);
        mockMvc.perform(get("/api/v1/public/tools").header("X-Forwarded-For", "10.7.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(10))
                .andExpect(jsonPath("$.data[?(@.code == 'SPELLING_CHECK')].name").value(org.hamcrest.Matchers.hasItem("التحقق الإملائي المرجعي")))
                .andExpect(jsonPath("$.data[?(@.route == '/tools/morphology')].status").value(org.hamcrest.Matchers.hasItem("LIMITED")));
        mockMvc.perform(get("/api/v1/admin/tools")).andExpect(status().isUnauthorized());
        Staff staff = staff();
        mockMvc.perform(get("/api/v1/admin/tools").header("Authorization", bearer(staff.editor))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/tools").header("Authorization", bearer(staff.owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tools.length()").value(10));
        mockMvc.perform(get("/api/v1/public/tools/word-analysis").param("q", "كتاب كبير").header("X-Forwarded-For", "10.7.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("هذه الأداة تقبل كلمة واحدة."));
        mockMvc.perform(get("/api/v1/public/tools/spelling-check").param("q", "كتاب ".repeat(30)).header("X-Forwarded-For", "10.7.0.1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("أطول")));
        mockMvc.perform(get("/api/v1/public/tools/root").param("q", "<script>alert(1)</script>").header("X-Forwarded-For", "10.7.0.1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/tools/root").param("q", "' or 1=1 --").header("X-Forwarded-For", "10.7.0.1"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/public/tools/grammar").param("q", "أعرب هذه الجملة الطويلة جدًا بكل تفاصيلها النحوية الآن").header("X-Forwarded-For", "10.7.0.1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rootDerivationsAndPatternsStayInsidePublishedKnowledge() throws Exception {
        Staff staff = staff();
        int mark = SEQUENCE.incrementAndGet();
        String rootLetters = rootOf(mark);
        String noun = rootLetters + "ة";
        String participle = participleOf(rootLetters);
        String unknown = "ب" + rootLetters + "ه";
        String citation = citation(staff);
        String root = publishRoot(staff, rootLetters);
        String verb = publishEntry(staff, rootLetters, id(root), "VERB", "فعل الاختبار", citation, null);
        String written = publishEntry(staff, noun, id(root), "NOUN", "اسم الاختبار", citation, null);
        String analysis = createAnalysis(staff.editor, id(verb));
        analysis = act(staff.editor, "/api/v1/admin/morphology/analyses/" + id(analysis) + "/submit", analysis);
        analysis = act(staff.reviewer, "/api/v1/admin/morphology/analyses/" + id(analysis) + "/verify", analysis);
        act(staff.publisher, "/api/v1/admin/morphology/analyses/" + id(analysis) + "/publish", analysis);

        mockMvc.perform(tool("/api/v1/public/tools/root", "q", rootLetters))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.heading").value("جذر موثّق"))
                .andExpect(jsonPath("$.data.result.href").value("/root/" + slug(root)))
                .andExpect(jsonPath("$.data.provenance[0].kind").value("EXACT_DICTIONARY"))
                .andExpect(jsonPath("$.data.provenance[0].label").value("مطابقة معجمية"));
        mockMvc.perform(tool("/api/v1/public/tools/root", "q", noun))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.heading").value("جذر موثّق"))
                .andExpect(jsonPath("$.data.result.root").value(rootLetters));
        mockMvc.perform(tool("/api/v1/public/tools/root", "q", unknown))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EMPTY"))
                .andExpect(jsonPath("$.data.result.heading").value("لا يوجد جذر موثّق"))
                .andExpect(jsonPath("$.data.result.root").isEmpty());
        mockMvc.perform(tool("/api/v1/public/tools/root", "q", participle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.heading").value("جذر محتمل"))
                .andExpect(jsonPath("$.data.result.root").value(rootLetters))
                .andExpect(jsonPath("$.data.provenance[0].kind").value("RULE_DERIVED"))
                .andExpect(jsonPath("$.data.provenance[0].label").value("مستنتج بقاعدة"))
                .andExpect(jsonPath("$.data.limitations[0]").value(org.hamcrest.Matchers.containsString("محتمل")));

        String derivations = body(mockMvc.perform(tool("/api/v1/public/tools/derivations", "q", rootLetters)).andExpect(status().isOk()).andReturn());
        assertThat(derivations).contains("فعل").contains("اسم").contains(noun).doesNotContain(participle);
        assertThat(JsonPath.<List<Object>>read(derivations, "$.data.result.groups[*].entries[*]")).hasSizeLessThanOrEqualTo(40);

        mockMvc.perform(tool("/api/v1/public/tools/patterns", "q", "فاعل"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.patterns[?(@.code == 'FA3IL')].radicalCount").value(org.hamcrest.Matchers.hasItem(3)));
        mockMvc.perform(tool("/api/v1/public/tools/patterns", "q", "فعلل"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.patterns[?(@.code == 'FA3LALA')].radicalCount").value(org.hamcrest.Matchers.hasItem(4)));
        mockMvc.perform(tool("/api/v1/public/tools/patterns", "q", participle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provenance[0].kind").value("RULE_DERIVED"))
                .andExpect(jsonPath("$.data.limitations[0]").value(org.hamcrest.Matchers.containsString("لا يعني أن كل كلمة")));
        mockMvc.perform(tool("/api/v1/public/tools/patterns", "q", unknown))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EMPTY"));
        mockMvc.perform(get("/api/v1/public/tools/patterns").header("X-Forwarded-For", "10.7.1." + mark))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.patterns[?(@.code == 'FA3LALA')]").isNotEmpty());

        long version = jdbc.queryForObject("select version from morphology_rule where code = 'R-FA3IL'", Long.class);
        try {
            mockMvc.perform(post("/api/v1/admin/morphology/rules/R-FA3IL").header("Authorization", bearer(staff.owner)).contentType(MediaType.APPLICATION_JSON).content("""
                    {"enabled":false,"version":%d}
                    """.formatted(version))).andExpect(status().isOk());
            mockMvc.perform(tool("/api/v1/public/tools/root", "q", participle))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.result.heading").value("لا يوجد جذر موثّق"));
        } finally {
            long restored = jdbc.queryForObject("select version from morphology_rule where code = 'R-FA3IL'", Long.class);
            mockMvc.perform(post("/api/v1/admin/morphology/rules/R-FA3IL").header("Authorization", bearer(staff.owner)).contentType(MediaType.APPLICATION_JSON).content("""
                    {"enabled":true,"version":%d}
                    """.formatted(restored))).andExpect(status().isOk());
        }
        assertThat(written).contains(noun);
    }

    @Test
    void wordComparisonRelationsAndSpellingDoNotInventMissingKnowledge() throws Exception {
        Staff staff = staff();
        int mark = SEQUENCE.incrementAndGet();
        String letters = rootOf(mark);
        String shared = letters + "ون";
        String other = letters + "ين";
        String opposite = letters + "ان";
        String hidden = letters + "وص";
        String multi = letters + "صف";
        String correct = letters + "صح";
        String mistake = letters + "خط";
        String contrast = letters + "بدل";
        String citation = citation(staff);
        String root = publishRoot(staff, letters);
        String noun = draftEntry(staff.editor, shared, id(root), "NOUN", "المعنى الأول");
        noun = addSense(staff.editor, noun, "المعنى الثاني", 2);
        noun = linkSense(staff.editor, noun, citation, 0);
        noun = linkSense(staff.editor, noun, citation, 1);
        String synonym = draftEntry(staff.editor, other, id(root), "NOUN", "معنى المرادف");
        synonym = linkSense(staff.editor, synonym, citation, 0);
        String antonym = draftEntry(staff.editor, opposite, id(root), "NOUN", "معنى الضد");
        antonym = linkSense(staff.editor, antonym, citation, 0);
        String draft = draftEntry(staff.editor, hidden, id(root), "NOUN", "مدخل غير منشور");
        noun = addRelation(staff.editor, noun, "SYNONYM", id(synonym), senseId(noun, 0), senseId(synonym, 0));
        noun = addRelation(staff.editor, noun, "ANTONYM", id(antonym), senseId(noun, 1), senseId(antonym, 0));
        noun = addRelation(staff.editor, noun, "RELATED", id(draft), senseId(noun, 0), senseId(draft, 0));
        noun = publish(staff, "/api/v1/admin/dictionary/entries/" + id(noun), noun);
        synonym = publish(staff, "/api/v1/admin/dictionary/entries/" + id(synonym), synonym);
        antonym = publish(staff, "/api/v1/admin/dictionary/entries/" + id(antonym), antonym);
        publishEntry(staff, multi, id(root), "NOUN", "اسم مشترك", citation, null);
        publishEntry(staff, multi, id(root), "ADJECTIVE", "وصف مستقل", citation, null);

        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        String analysis = body(mockMvc.perform(tool("/api/v1/public/tools/word-analysis", "q", multi)).andExpect(status().isOk()).andReturn());
        long queries = statistics.getQueryExecutionCount();
        assertThat(queries).isBetween(1L, 80L);
        assertThat(analysis).contains("وجدنا أكثر من مدخل للكلمة").contains("/word/").doesNotContain(hidden);
        assertThat(JsonPath.<List<Object>>read(analysis, "$.data.result.entries")).hasSize(2);

        mockMvc.perform(tool("/api/v1/public/tools/word-analysis", "q", "غغغغ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EMPTY"))
                .andExpect(jsonPath("$.data.limitations[0]").value(org.hamcrest.Matchers.containsString("غير متوفر")));
        mockMvc.perform(tool("/api/v1/public/tools/compare", "a", shared, "b", shared))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.sameWord").value(true))
                .andExpect(jsonPath("$.data.result.semanticDifference").value(org.hamcrest.Matchers.containsString("متطابقتان")));
        mockMvc.perform(tool("/api/v1/public/tools/compare", "a", shared, "b", other))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.semanticDifference").value(org.hamcrest.Matchers.containsString("مرادف")));
        mockMvc.perform(tool("/api/v1/public/tools/compare", "a", multi, "b", other))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.limitations[0]").value(org.hamcrest.Matchers.containsString("أكثر من مدخل")));
        mockMvc.perform(tool("/api/v1/public/tools/compare", "a", shared, "b", "غغغغ"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.missingSide").value(true))
                .andExpect(jsonPath("$.data.result.semanticDifference").value(org.hamcrest.Matchers.containsString("لا تتوفر")));
        mockMvc.perform(tool("/api/v1/public/tools/compare", "a", letters + "ة", "b", opposite))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.semanticDifference").value(org.hamcrest.Matchers.containsString("لا تتوفر")));

        String relations = body(mockMvc.perform(tool("/api/v1/public/tools/relations", "q", shared)).andExpect(status().isOk()).andReturn());
        assertThat(relations).contains(other).contains(opposite).doesNotContain(hidden);
        assertThat(JsonPath.<List<Object>>read(relations, "$.data.result.senses[0].synonyms")).isNotEmpty();
        assertThat(JsonPath.<List<Object>>read(relations, "$.data.result.senses[0].antonyms")).isEmpty();
        assertThat(JsonPath.<List<Object>>read(relations, "$.data.result.senses[1].antonyms")).isNotEmpty();

        String spelling = publishSpelling(staff, correct, mistake, contrast, citation);
        mockMvc.perform(tool("/api/v1/public/tools/spelling-check", "q", shared))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.verdict").value("موجودة في المعجم"))
                .andExpect(jsonPath("$.data.result.evidence[0].href").value(org.hamcrest.Matchers.startsWith("/word/")))
                .andExpect(jsonPath("$.data.provenance[*].kind").value(org.hamcrest.Matchers.hasItem("EXACT_DICTIONARY")));
        mockMvc.perform(tool("/api/v1/public/tools/spelling-check", "q", correct))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.verdict").value("صيغة موثّقة"))
                .andExpect(jsonPath("$.data.result.evidence[0].href").value("/spelling/rules/" + slug(spelling)));
        mockMvc.perform(tool("/api/v1/public/tools/spelling-check", "q", mistake))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.verdict").value("وردت كخطأ شائع"))
                .andExpect(jsonPath("$.data.result.evidence[0].recordedForm").value(correct));
        String unknownSpelling = body(mockMvc.perform(tool("/api/v1/public/tools/spelling-check", "q", contrast)).andExpect(status().isOk()).andReturn());
        assertThat(unknownSpelling).contains("مقابلة موثّقة").contains(correct).doesNotContain("الصحيح هو");
        String missing = body(mockMvc.perform(tool("/api/v1/public/tools/spelling-check", "q", letters + "مفق")).andExpect(status().isOk()).andReturn());
        assertThat(missing).contains("لم نعثر على هذه الصيغة").doesNotContain("الصحيح هو");
        if (missing.contains("هل تقصد؟")) {
            assertThat(missing).contains("ليست تصحيحًا مؤكدًا");
        }
    }

    @Test
    void grammarExplorerSearchesPublishedTermsWithoutParsing() throws Exception {
        Staff staff = staff();
        int mark = SEQUENCE.incrementAndGet();
        String topicWord = "باب" + arabicMark(mark);
        String ruleWord = "حكم" + arabicMark(mark);
        String conceptWord = "مصطلح" + arabicMark(mark);
        String alias = "بدل" + arabicMark(mark);
        String citation = citation(staff);
        String topic = body(mockMvc.perform(post("/api/v1/admin/grammar/topics").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"%s","summary":"ملخص الموضوع","category":"OTHER","displayOrder":1}
                """.formatted(topicWord))).andExpect(status().isOk()).andReturn());
        topic = publish(staff, "/api/v1/admin/grammar/topics/" + id(topic), topic);
        String rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","title":"%s","summary":"ملخص القاعدة","ruleText":"ترفع هذه القاعدة الاسم","displayOrder":1}
                """.formatted(id(topic), ruleWord))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/components").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"type":"CORE_RULE","body":"الاسم هنا مرفوع","displayOrder":1}
                """.formatted(version(rule)))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/grammar/rules/" + id(rule) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(rule), id(citation)))).andExpect(status().isOk()).andReturn());
        rule = publish(staff, "/api/v1/admin/grammar/rules/" + id(rule), rule);
        String concept = body(mockMvc.perform(post("/api/v1/admin/grammar/concepts").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"term":"%s","shortDefinition":"تعريف قصير","detailedDefinition":"تعريف مفصل"}
                """.formatted(conceptWord))).andExpect(status().isOk()).andReturn());
        concept = body(mockMvc.perform(post("/api/v1/admin/grammar/concepts/" + id(concept) + "/aliases").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"alias":"%s"}
                """.formatted(version(concept), alias))).andExpect(status().isOk()).andReturn());
        concept = body(mockMvc.perform(post("/api/v1/admin/grammar/concepts/" + id(concept) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(concept), id(citation)))).andExpect(status().isOk()).andReturn());
        concept = publish(staff, "/api/v1/admin/grammar/concepts/" + id(concept), concept);

        mockMvc.perform(tool("/api/v1/public/tools/grammar", "q", topicWord))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.items[?(@.kindLabel == 'موضوع')].href").value(org.hamcrest.Matchers.hasItem("/grammar/" + slug(topic))))
                .andExpect(jsonPath("$.data.limitations[0]").value(org.hamcrest.Matchers.containsString("ولا تعرب")));
        mockMvc.perform(tool("/api/v1/public/tools/grammar", "q", ruleWord))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.items[?(@.kindLabel == 'قاعدة')].href").value(org.hamcrest.Matchers.hasItem("/grammar/rules/" + slug(rule))));
        mockMvc.perform(tool("/api/v1/public/tools/grammar", "q", conceptWord))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.items[?(@.kindLabel == 'مفهوم')].title").value(org.hamcrest.Matchers.hasItem(conceptWord)));
        mockMvc.perform(tool("/api/v1/public/tools/grammar", "q", alias))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result.items[0].title").value(conceptWord));
        mockMvc.perform(tool("/api/v1/public/tools/grammar", "q", "مجهول" + arabicMark(mark)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EMPTY"))
                .andExpect(jsonPath("$.data.limitations[0]").value(org.hamcrest.Matchers.containsString("ولا تعرب")));
        assertThat(concept).contains(conceptWord);
    }

    @Test
    void graphStaysBoundedAndDropsArchivedContent() throws Exception {
        Staff staff = staff();
        int mark = SEQUENCE.incrementAndGet();
        String letters = rootOf(mark + 90);
        String aWord = letters + "الف";
        String bWord = letters + "باء";
        String cWord = letters + "جيم";
        String dWord = letters + "دال";
        String citation = citation(staff);
        String root = publishRoot(staff, letters);
        String a = publishEntry(staff, aWord, id(root), "NOUN", "طرف أول", citation, null);
        String b = publishEntry(staff, bWord, id(root), "NOUN", "طرف ثان", citation, null);
        String c = publishEntry(staff, cWord, id(root), "NOUN", "طرف ثالث", citation, null);
        String d = publishEntry(staff, dWord, id(root), "NOUN", "طرف رابع", citation, null);
        a = addRelation(staff.editor, a, "SYNONYM", id(b), senseId(a, 0), senseId(b, 0));
        b = addRelation(staff.editor, b, "RELATED", id(c), senseId(b, 0), senseId(c, 0));
        c = addRelation(staff.editor, c, "RELATED", id(d), senseId(c, 0), senseId(d, 0));
        a = publish(staff, "/api/v1/admin/dictionary/entries/" + id(a), a);
        b = publish(staff, "/api/v1/admin/dictionary/entries/" + id(b), b);
        c = publish(staff, "/api/v1/admin/dictionary/entries/" + id(c), c);

        String graph = body(mockMvc.perform(tool("/api/v1/public/tools/explore", "q", aWord)).andExpect(status().isOk()).andReturn());
        List<String> ids = JsonPath.read(graph, "$.data.result.nodes[*].id");
        assertThat(ids).doesNotHaveDuplicates().hasSizeLessThanOrEqualTo(50);
        assertThat(JsonPath.<List<Object>>read(graph, "$.data.result.list")).hasSize(ids.size());
        assertThat(graph).contains(bWord).contains(cWord).doesNotContain(dWord);
        assertThat(graph).contains("القائمة تعرض العقد نفسها");

        AtomicInteger ok = new AtomicInteger();
        AtomicInteger archived = new AtomicInteger();
        String entryId = id(b);
        long entryVersion = version(b);
        race(2, () -> {
            int read = statusOf(tool("/api/v1/public/tools/explore", "q", aWord));
            int write = statusOf(post("/api/v1/admin/dictionary/entries/" + entryId + "/archive").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(entryVersion)));
            if (read == 200) {
                ok.incrementAndGet();
            }
            if (write == 200 || write == 409) {
                archived.incrementAndGet();
            }
            if (read != 200 || (write != 200 && write != 409)) {
                throw new AssertionError("read=" + read + " write=" + write);
            }
        });
        assertThat(ok.get()).isEqualTo(2);
        String after = body(mockMvc.perform(tool("/api/v1/public/tools/explore", "q", aWord)).andExpect(status().isOk()).andReturn());
        if (archived.get() > 0 && after.contains("\"status\":\"OK\"") && jdbc.queryForObject("select status from lexical_entry where id = ?::uuid", String.class, entryId).equals("ARCHIVED")) {
            assertThat(after).doesNotContain(bWord);
        }
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder tool(String path, String... pairs) {
        var request = get(path).header("X-Forwarded-For", "10.8.0." + SEQUENCE.get());
        for (int index = 0; index < pairs.length; index += 2) {
            request = request.param(pairs[index], pairs[index + 1]);
        }
        return request;
    }

    private String publishSpelling(Staff staff, String correct, String mistake, String contrast, String citation) throws Exception {
        String topic = body(mockMvc.perform(post("/api/v1/admin/spelling/topics").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"إملاء %s","summary":"ملخص","displayOrder":1}
                """.formatted(correct))).andExpect(status().isOk()).andReturn());
        topic = body(mockMvc.perform(post("/api/v1/admin/spelling/topics/" + id(topic) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(topic), id(citation)))).andExpect(status().isOk()).andReturn());
        publish(staff, "/api/v1/admin/spelling/topics/" + id(topic), topic);
        String rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"topicId":"%s","title":"قاعدة %s","summary":"ملخص","coreRule":"تطابق الصيغة المسجلة","difficulty":"BEGINNER"}
                """.formatted(id(topic), correct))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/clauses").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"CONDITION","heading":"الشرط","body":"إذا وردت الصيغة في المثال"}
                """.formatted(version(rule)))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"CONTRAST","correctForm":"%s","incorrectForm":"%s","explanation":"مقابلة مسجلة","contextNote":"قد تصح صورة أخرى في سياق مختلف"}
                """.formatted(version(rule), correct, contrast))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/examples").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"COMMON_MISTAKE","correctForm":"%s","commonForm":"%s","reason":"خطأ مسجل","citationId":"%s"}
                """.formatted(version(rule), correct, mistake, id(citation)))).andExpect(status().isOk()).andReturn());
        rule = body(mockMvc.perform(post("/api/v1/admin/spelling/rules/" + id(rule) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(rule), id(citation)))).andExpect(status().isOk()).andReturn());
        return publish(staff, "/api/v1/admin/spelling/rules/" + id(rule), rule);
    }

    private String publishEntry(Staff staff, String lemma, String rootId, String pos, String definition, String citation, String form) throws Exception {
        String entry = draftEntry(staff.editor, lemma, rootId, pos, definition);
        if (form != null) {
            entry = addForm(staff.editor, entry, form);
        }
        entry = linkSense(staff.editor, entry, citation, 0);
        return publish(staff, "/api/v1/admin/dictionary/entries/" + id(entry), entry);
    }

    private String draftEntry(String token, String lemma, String rootId, String pos, String definition) throws Exception {
        String entry = body(mockMvc.perform(post("/api/v1/admin/dictionary/entries").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"lemma":"%s","rootId":"%s","partOfSpeech":"%s","gender":"MASCULINE"}
                """.formatted(lemma, rootId, pos))).andExpect(status().isOk()).andReturn());
        return addSense(token, entry, definition, 1);
    }

    private String addSense(String token, String entry, String definition, int order) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/senses").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"sense":{"definition":"%s","shortDefinition":"%s","displayOrder":%d}}
                """.formatted(version(entry), definition, definition, order))).andExpect(status().isOk()).andReturn());
    }

    private String addForm(String token, String entry, String form) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/forms").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"form":{"formType":"PLURAL","originalForm":"%s"}}
                """.formatted(version(entry), form))).andExpect(status().isOk()).andReturn());
    }

    private String linkSense(String token, String entry, String citation, int index) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/senses/" + senseId(entry, index) + "/citations").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"citationId":"%s"}
                """.formatted(version(entry), id(citation)))).andExpect(status().isOk()).andReturn());
    }

    private String addRelation(String token, String entry, String type, String targetId, String sourceSense, String targetSense) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/dictionary/entries/" + id(entry) + "/relations").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"relation":{"relationType":"%s","targetEntryId":"%s","sourceSenseId":"%s","targetSenseId":"%s","verificationLevel":"REVIEWED"}}
                """.formatted(version(entry), type, targetId, sourceSense, targetSense))).andExpect(status().isOk()).andReturn());
    }

    private String publish(Staff staff, String path, String resource) throws Exception {
        resource = act(staff.editor, path + "/submit", resource);
        resource = act(staff.reviewer, path + "/verify", resource);
        return act(staff.publisher, path + "/publish", resource);
    }

    private String publishRoot(Staff staff, String original) throws Exception {
        String root = body(mockMvc.perform(post("/api/v1/admin/dictionary/roots").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"original":"%s"}
                """.formatted(original))).andExpect(status().isOk()).andReturn());
        return publish(staff, "/api/v1/admin/dictionary/roots/" + id(root), root);
    }

    private String citation(Staff staff) throws Exception {
        String source = body(mockMvc.perform(post("/api/v1/admin/sources").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"sourceType":"DICTIONARY","title":"مقاييس اللغة","author":"ابن فارس","edition":"اختبار","licenseType":"PUBLIC_DOMAIN","publicDomain":true,"attributionText":"اختبار"}
                """)).andExpect(status().isOk()).andReturn());
        publish(staff, "/api/v1/admin/sources/" + id(source) + "", source);
        return body(mockMvc.perform(post("/api/v1/admin/sources/" + id(source) + "/citations").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"pageFrom":1,"pageTo":2,"entryLabel":"اختبار"}
                """)).andExpect(status().isOk()).andReturn());
    }

    private String createAnalysis(String token, String entryId) throws Exception {
        return body(mockMvc.perform(post("/api/v1/admin/morphology/analyses").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("""
                {"lexicalEntryId":"%s","patternId":"%s","derivation":"ROOT_DERIVED","verbClass":"SOUND","imperfectVowel":"DAMMA","notes":"تحليل اختبار الأدوات"}
                """.formatted(entryId, FA3ALA))).andExpect(status().isOk()).andReturn());
    }

    private Staff staff() throws Exception {
        Tokens owner = login("owner", "Owner-Pass-123!");
        int sequence = SEQUENCE.incrementAndGet();
        return new Staff(owner.accessToken(), member(owner, "s7e" + sequence, EDITOR_ROLE, "Editor-Pass-123!"), member(owner, "s7r" + sequence, REVIEWER_ROLE, "Review-Pass-123!"), member(owner, "s7p" + sequence, PUBLISHER_ROLE, "Publish-Pass-123!"), member(owner, "s7a" + sequence, AUDITOR_ROLE, "Audit-Pass-123!"));
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

    private static String rootOf(int mark) {
        String letters = "بتثجحخدذرزسشصضطظعغفقكلمنهوي";
        int first = mark % letters.length();
        int second = (mark + 5) % letters.length();
        int third = (mark + 13) % letters.length();
        if (second == first) {
            second = (second + 1) % letters.length();
        }
        if (third == first || third == second) {
            third = (third + 2) % letters.length();
        }
        return "" + letters.charAt(first) + letters.charAt(second) + letters.charAt(third);
    }

    private static String participleOf(String root) {
        return root.substring(0, 1) + "ا" + root.substring(1);
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

    private static String senseId(String payload, int index) {
        return JsonPath.read(payload, "$.data.senses[" + index + "].id");
    }

    private static long version(String payload) {
        return ((Number) JsonPath.read(payload, "$.data.version")).longValue();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private record Tokens(String accessToken, String refreshToken) {
    }

    private record Staff(String owner, String editor, String reviewer, String publisher, String auditor) {
    }
}
