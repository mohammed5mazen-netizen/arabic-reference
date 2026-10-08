package com.mrsoft.arabicreference.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mrsoft.arabicreference.IntegrationContainers;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
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
class LearningIntegrationTest {

    private static final UUID EDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000003");
    private static final UUID REVIEWER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000004");
    private static final UUID PUBLISHER_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000005");
    private static final UUID AUDITOR_ROLE = UUID.fromString("a0000000-0000-4000-8000-000000000006");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(900);
    private static final AtomicInteger ADDRESSES = new AtomicInteger(1);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.rate-limit.login", () -> "1000");
        registry.add("app.admin.rate-limit.refresh", () -> "1000");
        registry.add("app.learning.rate-limit-per-minute", () -> "6");
        registry.add("app.learning.attempt-ttl", () -> "30m");
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> "Owner-Pass-123!");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MeterRegistry meters;

    @Test
    void publishedLearningStaysAnonymousAndQuizAnswersStayHidden() throws Exception {
        Integer version14 = jdbc.queryForObject("select count(*) from flyway_schema_history where success = true and version = '14'", Integer.class);
        assertThat(version14).isEqualTo(1);
        mockMvc.perform(get("/api/v1/public/learning/paths")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/learning/paths")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/public/learning/attempts/not-a-token/submit").contentType(MediaType.APPLICATION_JSON).content("{\"answers\":[]}")).andExpect(status().isNotFound());

        Staff staff = staff();
        mockMvc.perform(post("/api/v1/admin/learning/paths").header("Authorization", bearer(staff.auditor)).contentType(MediaType.APPLICATION_JSON).content(pathBody("مسار مراجع", 1))).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/admin/learning/paths").header("Authorization", bearer(staff.editor))).andExpect(status().isOk());

        int mark = SEQUENCE.incrementAndGet();
        String title = "زلزالتعليمي" + mark;
        String path = ok(staff.editor, "/api/v1/admin/learning/paths", pathBody(title, mark));
        String pathId = id(path);
        long version = version(path);
        String withUnit = ok(staff.editor, "/api/v1/admin/learning/paths/" + pathId + "/units", """
                {"version":%d,"title":"وحدة الفاعل","summary":"وحدة اختبار","displayOrder":1}
                """.formatted(version));
        String unitId = text(withUnit, "$.data.units[0].id");
        version = version(withUnit);
        String withLesson = ok(staff.editor, "/api/v1/admin/learning/units/" + unitId + "/lessons", """
                {"version":%d,"title":"%s","summary":"درس اختبار للفاعل","estimatedMinutes":12,"displayOrder":1}
                """.formatted(version, title));
        String lessonId = text(withLesson, "$.data.units[0].lessons[0].id");
        String lessonSlug = text(withLesson, "$.data.units[0].lessons[0].slug");
        String pathSlug = text(withLesson, "$.data.slug");
        version = version(withLesson);
        version = version(ok(staff.editor, "/api/v1/admin/learning/lessons/" + lessonId + "/objectives", """
                {"version":%d,"text":"تعريف الفاعل في مثال تعليمي","displayOrder":1}
                """.formatted(version)));
        version = version(ok(staff.editor, "/api/v1/admin/learning/lessons/" + lessonId + "/sections", """
                {"version":%d,"type":"EXPLANATION","heading":"الشرح","body":"الفاعل اسم مرفوع يدل على من فعل الفعل.","exampleKind":"NONE","displayOrder":1}
                """.formatted(version)));
        version = version(ok(staff.editor, "/api/v1/admin/learning/lessons/" + lessonId + "/sections", """
                {"version":%d,"type":"EXAMPLE","heading":"مثال","body":"كتب الطالب الدرس.","exampleKind":"CONSTRUCTED","displayOrder":2}
                """.formatted(version)));
        version = version(ok(staff.editor, "/api/v1/admin/learning/lessons/" + lessonId + "/activities", """
                {"version":%d,"type":"READ","title":"اقرأ الشرح","instructions":"اقرأ قسم الشرح ثم أجب عن الاختبار.","displayOrder":1}
                """.formatted(version)));
        mockMvc.perform(post("/api/v1/admin/learning/lessons/" + lessonId + "/references").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"kind":"GRAMMAR_RULE","slug":"missing-rule","note":"مرجع"}
                """.formatted(version))).andExpect(status().isBadRequest());
        String withQuiz = ok(staff.editor, "/api/v1/admin/learning/lessons/" + lessonId + "/quiz", """
                {"version":%d,"title":"اختبار الفاعل","passingScore":70,"maxAttempts":3}
                """.formatted(version));
        String quizId = text(withQuiz, "$.data.units[0].lessons[0].quiz.id");
        version = version(withQuiz);
        mockMvc.perform(post("/api/v1/admin/learning/lessons/" + lessonId + "/quiz").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"title":"درجة خارج المدى","passingScore":101}
                """.formatted(version))).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/learning/quizzes/" + quizId + "/questions").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"prompt":"ناقص","explanation":"ناقص","type":"MULTIPLE_CHOICE","difficulty":"BEGINNER","displayOrder":1,"options":[{"label":"واحد","correct":true}]}
                """.formatted(version))).andExpect(status().isBadRequest());
        version = version(ok(staff.editor, "/api/v1/admin/learning/quizzes/" + quizId + "/questions", question(version, 1, "MULTIPLE_CHOICE", "ما نوع كلمة الطالب في المثال؟", "الاسم هو الفاعل.", """
                [{"label":"اسم","correct":true},{"label":"حرف","correct":false}]
                """)));
        version = version(ok(staff.editor, "/api/v1/admin/learning/quizzes/" + quizId + "/questions", question(version, 2, "TRUE_FALSE", "الطالب هو الذي فعل الكتابة.", "المثال التعليمي يبيّن ذلك.", """
                [{"label":"صواب","correct":true},{"label":"خطأ","correct":false}]
                """)));
        String asked = ok(staff.editor, "/api/v1/admin/learning/quizzes/" + quizId + "/questions", question(version, 3, "MULTIPLE_SELECT", "حدد كل الأسماء.", "الاسم والفعل معًا، والحرف ليس منهما في هذا السؤال.", """
                [{"label":"الطالب","correct":true},{"label":"الدرس","correct":true},{"label":"في","correct":false}]
                """));
        version = version(asked);
        String choiceId = text(asked, "$.data.units[0].lessons[0].quiz.questions[0].options[0].id");
        String falseChoice = text(asked, "$.data.units[0].lessons[0].quiz.questions[0].options[1].id");
        String trueId = text(asked, "$.data.units[0].lessons[0].quiz.questions[1].options[0].id");
        String falseId = text(asked, "$.data.units[0].lessons[0].quiz.questions[1].options[1].id");
        String multiA = text(asked, "$.data.units[0].lessons[0].quiz.questions[2].options[0].id");
        String multiB = text(asked, "$.data.units[0].lessons[0].quiz.questions[2].options[1].id");
        String multiC = text(asked, "$.data.units[0].lessons[0].quiz.questions[2].options[2].id");
        String q1 = text(asked, "$.data.units[0].lessons[0].quiz.questions[0].id");
        String q2 = text(asked, "$.data.units[0].lessons[0].quiz.questions[1].id");
        String q3 = text(asked, "$.data.units[0].lessons[0].quiz.questions[2].id");
        assertThat(asked).contains("\"correct\":true");

        mockMvc.perform(get("/api/v1/public/learning/paths/" + pathSlug)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/public/search").param("q", title).param("type", "learning")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
        mockMvc.perform(post("/api/v1/public/learning/quizzes/" + quizId + "/attempts").header("X-Forwarded-For", ip())).andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/admin/learning/paths/" + pathId + "/verify").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version))).andExpect(status().isForbidden());
        version = version(ok(staff.editor, "/api/v1/admin/learning/paths/" + pathId + "/submit", versionBody(version)));
        mockMvc.perform(post("/api/v1/admin/learning/paths/" + pathId + "/verify").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version))).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/admin/learning/paths/" + pathId + "/verify").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version))).andExpect(status().isForbidden());
        version = version(ok(staff.reviewer, "/api/v1/admin/learning/paths/" + pathId + "/changes", versionBody(version)));
        version = version(ok(staff.editor, "/api/v1/admin/learning/lessons/" + lessonId + "/objectives", """
                {"version":%d,"text":"التمييز بين الفاعل والمفعول به","displayOrder":2}
                """.formatted(version)));
        version = version(ok(staff.editor, "/api/v1/admin/learning/paths/" + pathId + "/submit", versionBody(version)));
        mockMvc.perform(post("/api/v1/admin/learning/paths/" + pathId + "/publish").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version))).andExpect(status().isForbidden());
        version = version(ok(staff.reviewer, "/api/v1/admin/learning/paths/" + pathId + "/verify", versionBody(version)));
        mockMvc.perform(post("/api/v1/admin/learning/paths/" + pathId + "/publish").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version))).andExpect(status().isForbidden());
        String published = ok(staff.publisher, "/api/v1/admin/learning/paths/" + pathId + "/publish", versionBody(version));
        version = version(published);
        assertThat(text(published, "$.data.status")).isEqualTo("PUBLISHED");

        mockMvc.perform(get("/api/v1/public/learning/paths/" + pathSlug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.difficultyLabel").value("مبتدئ"))
                .andExpect(jsonPath("$.data.units[0].lessons[0].title").value(title));
        String lesson = mockMvc.perform(get("/api/v1/public/learning/lessons/" + lessonSlug)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(lesson).contains("مثال تعليمي");
        assertThat(lesson).doesNotContain("\"correct\"");
        assertThat(lesson).contains("اقرأ الشرح");
        assertThat(meters.counter("learning.lesson.views").count()).isPositive();

        MvcResult started = mockMvc.perform(post("/api/v1/public/learning/quizzes/" + quizId + "/attempts").header("X-Forwarded-For", ip())).andExpect(status().isOk()).andReturn();
        String attempt = started.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(attempt).doesNotContain("\"correct\"");
        assertThat(attempt).doesNotContain("explanation");
        String token = text(attempt, "$.data.token");
        assertThat(token).hasSizeGreaterThan(40);
        String wrong = submit(token, "wrong-" + mark, answers(q1, falseChoice, q2, falseId, q3, multiC));
        assertThat(text(wrong, "$.data.passed")).isEqualTo("false");
        assertThat(wrong).contains("الاسم هو الفاعل.");
        String same = submit(token, "wrong-" + mark, answers(q1, falseChoice, q2, falseId, q3, multiC));
        assertThat(text(same, "$.data.score")).isEqualTo(text(wrong, "$.data.score"));
        mockMvc.perform(post("/api/v1/public/learning/attempts/" + token + "/submit").header("X-Forwarded-For", ip()).contentType(MediaType.APPLICATION_JSON).content("{\"idempotencyKey\":\"other-" + mark + "\",\"answers\":[]}")).andExpect(status().isConflict());

        MvcResult passedStart = mockMvc.perform(post("/api/v1/public/learning/quizzes/" + quizId + "/attempts").header("X-Forwarded-For", ip())).andExpect(status().isOk()).andReturn();
        String passingToken = text(passedStart.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.data.token");
        String passed = submit(passingToken, "pass-" + mark, answers(q1, choiceId, q2, trueId, q3, multiA + "\",\"" + multiB));
        assertThat(text(passed, "$.data.score")).isEqualTo("100");
        assertThat(text(passed, "$.data.passed")).isEqualTo("true");
        assertThat(meters.counter("learning.quiz.passed").count()).isPositive();

        MvcResult partialStart = mockMvc.perform(post("/api/v1/public/learning/quizzes/" + quizId + "/attempts").header("X-Forwarded-For", ip())).andExpect(status().isOk()).andReturn();
        String partialToken = text(partialStart.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.data.token");
        String partial = submit(partialToken, "partial-" + mark, answers(q1, choiceId, q2, trueId, q3, multiA));
        assertThat(text(partial, "$.data.score")).isEqualTo("66");
        assertThat(text(partial, "$.data.passed")).isEqualTo("false");

        MvcResult guarded = mockMvc.perform(post("/api/v1/public/learning/quizzes/" + quizId + "/attempts").header("X-Forwarded-For", ip())).andExpect(status().isOk()).andReturn();
        String guardedToken = text(guarded.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.data.token");
        mockMvc.perform(post("/api/v1/public/learning/attempts/" + guardedToken + "/submit").header("X-Forwarded-For", ip()).contentType(MediaType.APPLICATION_JSON).content(answers(q1, choiceId, q2, trueId, UUID.randomUUID().toString(), multiA))).andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/public/learning/attempts/" + guardedToken + "/submit").header("X-Forwarded-For", ip()).contentType(MediaType.APPLICATION_JSON).content(answers(q1, choiceId + "\",\"" + choiceId, q2, trueId, q3, multiA))).andExpect(status().isBadRequest());
        jdbc.update("update learning_question set explanation = ? where id = ?", "تغير لاحقًا", UUID.fromString(q1));
        String stable = submit(guardedToken, "stable-" + mark, answers(q1, choiceId, q2, trueId, q3, multiA + "\",\"" + multiB));
        assertThat(stable).contains("الاسم هو الفاعل.");
        assertThat(stable).doesNotContain("تغير لاحقًا");
        Integer attemptAudits = jdbc.queryForObject("select count(*) from admin_audit_event where target_type = 'learning_attempt'", Integer.class);
        assertThat(attemptAudits).isZero();
        Integer created = jdbc.queryForObject("select count(*) from admin_audit_event where event_type = 'LEARNING_PATH_CREATED'", Integer.class);
        assertThat(created).isPositive();

        MvcResult expiredStart = mockMvc.perform(post("/api/v1/public/learning/quizzes/" + quizId + "/attempts").header("X-Forwarded-For", ip())).andExpect(status().isOk()).andReturn();
        String expiredToken = text(expiredStart.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.data.token");
        jdbc.update("update learning_attempt set expires_at = now() - interval '1 minute' where token_hash is not null and status = 'OPEN'");
        mockMvc.perform(post("/api/v1/public/learning/attempts/" + expiredToken + "/submit").header("X-Forwarded-For", ip()).contentType(MediaType.APPLICATION_JSON).content(answers(q1, choiceId, q2, trueId, q3, multiA + "\",\"" + multiB))).andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/v1/public/search").param("q", title).param("type", "learning"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[*].type", hasItem("LESSON")));
        String burst = ip();
        for (int attemptNumber = 0; attemptNumber < 6; attemptNumber++) {
            mockMvc.perform(post("/api/v1/public/learning/quizzes/" + quizId + "/attempts").header("X-Forwarded-For", burst)).andExpect(status().isOk());
        }
        mockMvc.perform(post("/api/v1/public/learning/quizzes/" + quizId + "/attempts").header("X-Forwarded-For", burst)).andExpect(status().isTooManyRequests());

        String archived = ok(staff.publisher, "/api/v1/admin/learning/paths/" + pathId + "/archive", versionBody(version));
        assertThat(text(archived, "$.data.status")).isEqualTo("ARCHIVED");
        mockMvc.perform(get("/api/v1/public/learning/lessons/" + lessonSlug)).andExpect(status().isNotFound());
        mockMvc.perform(post("/api/v1/public/learning/quizzes/" + quizId + "/attempts").header("X-Forwarded-For", ip())).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/public/search").param("q", title).param("type", "learning")).andExpect(status().isOk()).andExpect(jsonPath("$.data.total").value(0));
        Integer revisions = jdbc.queryForObject("select count(*) from learning_revision where path_id = ?", Integer.class, UUID.fromString(pathId));
        assertThat(revisions).isGreaterThanOrEqualTo(1);
    }

    @Test
    void quotedSectionCannotPublishWithoutAReferenceAndRacesStaySingleWinner() throws Exception {
        Staff staff = staff();
        Built own = ready(staff, "ملكيةالمؤلف" + SEQUENCE.incrementAndGet(), staff.owner);
        long ownVersion = version(ok(staff.owner, "/api/v1/admin/learning/paths/" + own.pathId + "/submit", versionBody(own.version)));
        mockMvc.perform(post("/api/v1/admin/learning/paths/" + own.pathId + "/verify").header("Authorization", bearer(staff.owner)).contentType(MediaType.APPLICATION_JSON).content(versionBody(ownVersion))).andExpect(status().isForbidden());

        Built quoted = ready(staff, "اقتباستنشر" + SEQUENCE.incrementAndGet(), staff.editor);
        long version = version(ok(staff.editor, "/api/v1/admin/learning/lessons/" + quoted.lessonId + "/sections", """
                {"version":%d,"type":"EXAMPLE","heading":"اقتباس","body":"نص قصير بلا مصدر.","exampleKind":"QUOTATION","displayOrder":2}
                """.formatted(quoted.version)));
        version = version(ok(staff.editor, "/api/v1/admin/learning/paths/" + quoted.pathId + "/submit", versionBody(version)));
        version = version(ok(staff.reviewer, "/api/v1/admin/learning/paths/" + quoted.pathId + "/verify", versionBody(version)));
        mockMvc.perform(post("/api/v1/admin/learning/paths/" + quoted.pathId + "/publish").header("Authorization", bearer(staff.publisher)).contentType(MediaType.APPLICATION_JSON).content(versionBody(version))).andExpect(status().isBadRequest());

        Built review = ready(staff, "سباقمراجعة" + SEQUENCE.incrementAndGet(), staff.editor);
        long reviewVersion = version(ok(staff.editor, "/api/v1/admin/learning/paths/" + review.pathId + "/submit", versionBody(review.version)));
        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        race(2, () -> count(statusOf(post("/api/v1/admin/learning/paths/" + review.pathId + "/verify").header("Authorization", bearer(staff.reviewer)).contentType(MediaType.APPLICATION_JSON).content(versionBody(reviewVersion))), success, conflict));
        assertThat(success).hasValue(1);
        assertThat(conflict).hasValue(1);

        Built edits = publishable(staff, "سباقتعديل" + SEQUENCE.incrementAndGet());
        AtomicInteger edited = new AtomicInteger();
        AtomicInteger editConflict = new AtomicInteger();
        race(2, () -> count(statusOf(post("/api/v1/admin/learning/lessons/" + edits.lessonId + "/objectives").header("Authorization", bearer(staff.editor)).contentType(MediaType.APPLICATION_JSON).content("""
                {"version":%d,"text":"هدف متزامن","displayOrder":2}
                """.formatted(edits.version))), edited, editConflict));
        assertThat(edited).hasValue(1);
        assertThat(editConflict).hasValue(1);

        MvcResult first = mockMvc.perform(post("/api/v1/public/learning/quizzes/" + edits.quizId + "/attempts").header("X-Forwarded-For", ip())).andExpect(status().isOk()).andReturn();
        String token = text(first.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.data.token");
        String body = answers(edits.questionId, edits.optionId);
        AtomicInteger submitted = new AtomicInteger();
        AtomicInteger submitConflict = new AtomicInteger();
        race(2, () -> count(statusOf(post("/api/v1/public/learning/attempts/" + token + "/submit").header("X-Forwarded-For", ip()).contentType(MediaType.APPLICATION_JSON).content(body.replace("same-key", "race-" + ADDRESSES.incrementAndGet()))), submitted, submitConflict));
        assertThat(submitted).hasValue(1);
        assertThat(submitConflict).hasValue(1);
        String snapshot = jdbc.queryForObject("select published_snapshot::text from learning_path where id = ?", String.class, UUID.fromString(edits.pathId));
        assertThat(snapshot).contains("هذا مثال تعليمي.");
        long current = jdbc.queryForObject("select version from learning_path where id = ?", Long.class, UUID.fromString(edits.pathId));
        String revised = ok(staff.editor, "/api/v1/admin/learning/questions/" + edits.questionId, """
                {"version":%d,"prompt":"صياغة لاحقة لا تظهر للعامة"}
                """.formatted(current));
        assertThat(text(revised, "$.data.status")).isEqualTo("DRAFT");
        String stillPublic = jdbc.queryForObject("select published_snapshot::text from learning_path where id = ?", String.class, UUID.fromString(edits.pathId));
        assertThat(stillPublic).contains("هذا مثال تعليمي.");
        assertThat(stillPublic).doesNotContain("صياغة لاحقة");
    }

    private Built ready(Staff staff, String title, String actor) throws Exception {
        String path = ok(actor, "/api/v1/admin/learning/paths", pathBody(title, SEQUENCE.incrementAndGet()));
        String pathId = id(path);
        String withUnit = ok(actor, "/api/v1/admin/learning/paths/" + pathId + "/units", """
                {"version":%d,"title":"وحدة","summary":"ملخص الوحدة","displayOrder":1}
                """.formatted(version(path)));
        String unitId = text(withUnit, "$.data.units[0].id");
        String withLesson = ok(actor, "/api/v1/admin/learning/units/" + unitId + "/lessons", """
                {"version":%d,"title":"%s","summary":"ملخص الدرس","estimatedMinutes":8,"displayOrder":1}
                """.formatted(version(withUnit), title));
        String lessonId = text(withLesson, "$.data.units[0].lessons[0].id");
        long version = version(ok(actor, "/api/v1/admin/learning/lessons/" + lessonId + "/objectives", """
                {"version":%d,"text":"هدف الدرس","displayOrder":1}
                """.formatted(version(withLesson))));
        version = version(ok(actor, "/api/v1/admin/learning/lessons/" + lessonId + "/sections", """
                {"version":%d,"type":"INTRODUCTION","heading":"تمهيد","body":"نص تعليمي قصير.","exampleKind":"NONE","displayOrder":1}
                """.formatted(version)));
        return new Built(pathId, lessonId, null, null, null, version);
    }

    private Built publishable(Staff staff, String title) throws Exception {
        Built draft = ready(staff, title, staff.editor);
        String withQuiz = ok(staff.editor, "/api/v1/admin/learning/lessons/" + draft.lessonId + "/quiz", """
                {"version":%d,"title":"اختبار","passingScore":100}
                """.formatted(draft.version));
        String quizId = text(withQuiz, "$.data.units[0].lessons[0].quiz.id");
        String asked = ok(staff.editor, "/api/v1/admin/learning/quizzes/" + quizId + "/questions", question(version(withQuiz), 1, "TRUE_FALSE", "هذا مثال تعليمي.", "لأنه مثال.", """
                [{"label":"صواب","correct":true},{"label":"خطأ","correct":false}]
                """));
        long version = version(ok(staff.editor, "/api/v1/admin/learning/paths/" + draft.pathId + "/submit", versionBody(version(asked))));
        version = version(ok(staff.reviewer, "/api/v1/admin/learning/paths/" + draft.pathId + "/verify", versionBody(version)));
        version = version(ok(staff.publisher, "/api/v1/admin/learning/paths/" + draft.pathId + "/publish", versionBody(version)));
        return new Built(draft.pathId, draft.lessonId, quizId, text(asked, "$.data.units[0].lessons[0].quiz.questions[0].id"), text(asked, "$.data.units[0].lessons[0].quiz.questions[0].options[0].id"), version);
    }

    private String question(long version, int order, String type, String prompt, String explanation, String options) {
        return """
                {"version":%d,"prompt":"%s","explanation":"%s","type":"%s","difficulty":"BEGINNER","displayOrder":%d,"options":%s}
                """.formatted(version, prompt, explanation, type, order, options.strip());
    }

    private String answers(String... pairs) {
        StringBuilder builder = new StringBuilder("{\"idempotencyKey\":\"same-key\",\"answers\":[");
        for (int index = 0; index < pairs.length; index += 2) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append("{\"questionId\":\"").append(pairs[index]).append("\",\"optionIds\":[");
            String[] options = pairs[index + 1].split("\",\"");
            for (int option = 0; option < options.length; option++) {
                if (option > 0) {
                    builder.append(',');
                }
                builder.append('"').append(options[option]).append('"');
            }
            builder.append("]}");
        }
        return builder.append("]}").toString();
    }

    private String submit(String token, String key, String body) throws Exception {
        return mockMvc.perform(post("/api/v1/public/learning/attempts/" + token + "/submit").header("X-Forwarded-For", ip()).contentType(MediaType.APPLICATION_JSON).content(body.replace("same-key", key))).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private String pathBody(String title, int order) {
        return """
                {"title":"%s","summary":"ملخص المسار","description":"وصف تعليمي مختصر للاختبار.","difficulty":"BEGINNER","estimatedMinutes":20,"displayOrder":%d}
                """.formatted(title, order);
    }

    private String ok(String token, String path, String json) throws Exception {
        return mockMvc.perform(post(path).header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    private Staff staff() throws Exception {
        Tokens owner = login("owner", "Owner-Pass-123!");
        int sequence = SEQUENCE.incrementAndGet();
        return new Staff(
                owner.accessToken(),
                member(owner, "s9e" + sequence, EDITOR_ROLE, "Editor-Pass-123!"),
                member(owner, "s9r" + sequence, REVIEWER_ROLE, "Review-Pass-123!"),
                member(owner, "s9p" + sequence, PUBLISHER_ROLE, "Publish-Pass-123!"),
                member(owner, "s9a" + sequence, AUDITOR_ROLE, "Audit-Pass-123!"));
    }

    private String member(Tokens owner, String username, UUID role, String password) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/v1/admin/users").header("Authorization", bearer(owner.accessToken())).contentType(MediaType.APPLICATION_JSON).content("""
                {"username":"%s","email":"%s@arabic-reference.test","displayName":"%s","roleIds":["%s"]}
                """.formatted(username, username, username, role))).andExpect(status().isOk()).andReturn();
        String temporary = JsonPath.read(created.getResponse().getContentAsString(), "$.data.temporaryPassword");
        return changePassword(login(username, temporary), temporary, password).accessToken();
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

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String versionBody(long version) {
        return "{\"version\":" + version + "}";
    }

    private static String id(String body) {
        return text(body, "$.data.id");
    }

    private static long version(String body) {
        return ((Number) JsonPath.read(body, "$.data.version")).longValue();
    }

    private static String text(String body, String path) {
        Object value = JsonPath.read(body, path);
        return String.valueOf(value);
    }

    private static String ip() {
        return "203.0.113." + ADDRESSES.incrementAndGet();
    }

    private record Staff(String owner, String editor, String reviewer, String publisher, String auditor) {
    }

    private record Tokens(String accessToken, String refreshToken) {
    }

    private record Built(String pathId, String lessonId, String quizId, String questionId, String optionId, long version) {
    }
}
