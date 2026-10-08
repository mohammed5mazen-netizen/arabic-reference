package com.mrsoft.arabicreference.learning.application;

import com.mrsoft.arabicreference.identity.application.AuditRecorder;
import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.learning.domain.ActivityType;
import com.mrsoft.arabicreference.learning.domain.Difficulty;
import com.mrsoft.arabicreference.learning.domain.DisplayOrder;
import com.mrsoft.arabicreference.learning.domain.QuestionPolicy;
import com.mrsoft.arabicreference.learning.domain.QuestionType;
import com.mrsoft.arabicreference.learning.domain.QuizScorer;
import com.mrsoft.arabicreference.learning.domain.ReferenceKind;
import com.mrsoft.arabicreference.learning.domain.SectionType;
import com.mrsoft.arabicreference.learning.infrastructure.LearningRateLimiter;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialGuards;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialWorkflow;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import com.mrsoft.arabicreference.linguistics.domain.text.ContentSlugs;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

@Service
public class LearningService {

    private static final ArabicTextNormalizer NORMALIZER = new ArabicTextNormalizer();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final JdbcTemplate jdbc;
    private final JsonMapper json;
    private final TimeProvider time;
    private final AuthorizationService authorization;
    private final AuditRecorder audit;
    private final PublishedReferencePort references;
    private final LearningSearchSource search;
    private final LearningPropertiesView properties;
    private final LearningRateLimiter limits;
    private final LearningMetrics metrics;
    private final Object cacheLock = new Object();
    private long cacheGeneration;
    private final Map<String, Object> cache = new LinkedHashMap<>(32, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
            return size() > 32;
        }
    };

    public LearningService(
            JdbcTemplate jdbc,
            JsonMapper json,
            TimeProvider time,
            AuthorizationService authorization,
            AuditRecorder audit,
            PublishedReferencePort references,
            LearningSearchSource search,
            LearningPropertiesView properties,
            LearningRateLimiter limits,
            LearningMetrics metrics) {
        this.jdbc = jdbc;
        this.json = json;
        this.time = time;
        this.authorization = authorization;
        this.audit = audit;
        this.references = references;
        this.search = search;
        this.properties = properties;
        this.limits = limits;
        this.metrics = metrics;
    }

    @Transactional
    public Map<String, Object> createPath(String title, String summary, String description, String difficulty, Integer minutes, int displayOrder, UUID prerequisiteId) {
        UUID actor = authorization.requireAccess().userId();
        UUID id = Ids.random();
        Instant now = time.now();
        Difficulty level = difficulty(difficulty);
        jdbc.update("""
                insert into learning_path (id, title, slug, summary, description, difficulty, estimated_minutes, status, display_order, revision, version, prerequisite_id, created_by, updated_by, created_at, updated_at)
                values (?, ?, ?, ?, ?, ?, ?, 'DRAFT', ?, 1, 0, ?, ?, ?, ?, ?)
                """, id, required(title, "title", 160), ContentSlugs.of(NORMALIZER.normalize(title).normalizedText(), id), required(summary, "summary", 500), required(description, "description", 4000), level.name(), minutes, displayOrder, prerequisiteId, actor, actor, timestamp(now), timestamp(now));
        audit.record(actor, AuditEventType.LEARNING_PATH_CREATED, "learning_path", id.toString(), Map.of("status", "DRAFT"));
        return adminPath(id);
    }

    @Transactional
    public Map<String, Object> addUnit(UUID pathId, long version, String title, String summary, int displayOrder) {
        UUID actor = mutate(pathId, version);
        UUID id = Ids.random();
        insert("insert into learning_unit (id, path_id, title, summary, display_order, version) values (?, ?, ?, ?, ?, 0)", id, pathId, required(title, "title", 160), required(summary, "summary", 500), displayOrder);
        audit.record(actor, AuditEventType.LEARNING_PATH_UPDATED, "learning_path", pathId.toString(), Map.of("unit", id.toString()));
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> addLesson(UUID unitId, long pathVersion, String title, String summary, Integer minutes, int displayOrder) {
        UUID pathId = pathOfUnit(unitId);
        UUID actor = mutate(pathId, pathVersion);
        UUID id = Ids.random();
        insert("insert into learning_lesson (id, unit_id, title, slug, summary, estimated_minutes, display_order, version) values (?, ?, ?, ?, ?, ?, ?, 0)", id, unitId, required(title, "title", 160), ContentSlugs.of(NORMALIZER.normalize(title).normalizedText(), id), required(summary, "summary", 500), minutes, displayOrder);
        audit.record(actor, AuditEventType.LESSON_CREATED, "learning_lesson", id.toString(), Map.of("path", pathId.toString()));
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> addObjective(UUID lessonId, long pathVersion, String statement, int displayOrder) {
        UUID pathId = pathOfLesson(lessonId);
        mutate(pathId, pathVersion);
        insert("insert into learning_objective (id, lesson_id, statement, display_order) values (?, ?, ?, ?)", Ids.random(), lessonId, required(statement, "statement", 300), displayOrder);
        audit.record(authorization.requireAccess().userId(), AuditEventType.LESSON_UPDATED, "learning_lesson", lessonId.toString(), Map.of("scope", "learning"));
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> addSection(UUID lessonId, long pathVersion, String type, String heading, String body, String exampleKind, int displayOrder) {
        UUID pathId = pathOfLesson(lessonId);
        mutate(pathId, pathVersion);
        SectionType sectionType = enumValue(SectionType.class, type, "type");
        String kind = exampleKind == null || exampleKind.isBlank() ? "NONE" : exampleKind;
        if (!kind.equals("NONE") && !kind.equals("CONSTRUCTED") && !kind.equals("QUOTATION")) {
            throw invalid("exampleKind", "نوع المثال غير مدعوم.");
        }
        insert("insert into learning_section (id, lesson_id, section_type, heading, body, example_kind, display_order) values (?, ?, ?, ?, ?, ?, ?)", Ids.random(), lessonId, sectionType.name(), required(heading, "heading", 160), required(body, "body", 1200), kind, displayOrder);
        audit.record(authorization.requireAccess().userId(), AuditEventType.LESSON_UPDATED, "learning_lesson", lessonId.toString(), Map.of("scope", "learning"));
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> addReference(UUID lessonId, long pathVersion, String kind, String slug, String note) {
        UUID pathId = pathOfLesson(lessonId);
        mutate(pathId, pathVersion);
        ReferenceKind referenceKind = enumValue(ReferenceKind.class, kind, "kind");
        if (references.resolve(referenceKind, slug).isEmpty()) {
            throw invalid("slug", "المرجع غير منشور في المعرفة.");
        }
        insert("insert into learning_reference (id, lesson_id, target_kind, target_slug, note) values (?, ?, ?, ?, ?)", Ids.random(), lessonId, referenceKind.name(), required(slug, "slug", 180), note == null || note.isBlank() ? null : required(note, "note", 280));
        audit.record(authorization.requireAccess().userId(), AuditEventType.LESSON_UPDATED, "learning_lesson", lessonId.toString(), Map.of("reference", referenceKind.name()));
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> addActivity(UUID lessonId, long pathVersion, String type, String title, String instructions, int displayOrder) {
        UUID pathId = pathOfLesson(lessonId);
        mutate(pathId, pathVersion);
        ActivityType activityType = enumValue(ActivityType.class, type, "type");
        insert("insert into learning_activity (id, lesson_id, activity_type, title, instructions, display_order) values (?, ?, ?, ?, ?, ?)", Ids.random(), lessonId, activityType.name(), required(title, "title", 160), required(instructions, "instructions", 800), displayOrder);
        audit.record(authorization.requireAccess().userId(), AuditEventType.LESSON_UPDATED, "learning_lesson", lessonId.toString(), Map.of("scope", "learning"));
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> addQuiz(UUID lessonId, UUID unitId, long pathVersion, String title, int passingScore, Integer maxAttempts) {
        if ((lessonId == null) == (unitId == null)) {
            throw invalid("quiz", "الاختبار يرتبط بدرس أو بوحدة واحدة.");
        }
        UUID pathId = lessonId != null ? pathOfLesson(lessonId) : pathOfUnit(unitId);
        UUID actor = mutate(pathId, pathVersion);
        if (passingScore < 0 || passingScore > 100) {
            throw invalid("passingScore", "درجة النجاح بين 0 و100.");
        }
        UUID id = Ids.random();
        insert("insert into learning_quiz (id, lesson_id, unit_id, title, passing_score, max_attempts, content_version, version) values (?, ?, ?, ?, ?, ?, 1, 0)", id, lessonId, unitId, required(title, "title", 160), passingScore, maxAttempts);
        audit.record(actor, AuditEventType.QUIZ_CREATED, "learning_quiz", id.toString(), Map.of("path", pathId.toString()));
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> addQuestion(UUID quizId, long pathVersion, String prompt, String explanation, String type, String difficulty, int displayOrder, String knowledgeKind, String knowledgeSlug, List<OptionInput> options) {
        UUID pathId = pathOfQuiz(quizId);
        UUID actor = mutate(pathId, pathVersion);
        QuestionType questionType = enumValue(QuestionType.class, type, "type");
        Difficulty level = difficulty(difficulty);
        if (knowledgeKind != null && !knowledgeKind.isBlank()) {
            ReferenceKind kind = enumValue(ReferenceKind.class, knowledgeKind, "knowledgeKind");
            if (references.resolve(kind, knowledgeSlug).isEmpty()) {
                throw invalid("knowledgeSlug", "مرجع السؤال غير منشور.");
            }
        }
        try {
            QuestionPolicy.check(questionType, options.stream().map(OptionInput::correct).toList());
        } catch (IllegalArgumentException exception) {
            throw invalid("options", "خيارات السؤال لا تطابق نوعه.");
        }
        UUID questionId = Ids.random();
        insert("insert into learning_question (id, quiz_id, prompt, explanation, question_type, difficulty, display_order, knowledge_kind, knowledge_slug) values (?, ?, ?, ?, ?, ?, ?, ?, ?)", questionId, quizId, required(prompt, "prompt", 500), required(explanation, "explanation", 800), questionType.name(), level.name(), displayOrder, blankToNull(knowledgeKind), blankToNull(knowledgeSlug));
        int order = 1;
        for (OptionInput option : options) {
            insert("insert into learning_question_option (id, question_id, label, correct, display_order) values (?, ?, ?, ?, ?)", Ids.random(), questionId, required(option.label(), "label", 300), option.correct(), order++);
        }
        jdbc.update("update learning_quiz set content_version = content_version + 1, version = version + 1 where id = ?", quizId);
        audit.record(actor, AuditEventType.QUESTION_CREATED, "learning_question", questionId.toString(), Map.of("quiz", quizId.toString()));
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> reviseQuestion(UUID questionId, long pathVersion, String prompt) {
        UUID quizId = jdbc.query("select quiz_id from learning_question where id = ?", (row, index) -> row.getObject(1, UUID.class), questionId).stream().findFirst().orElseThrow(this::missing);
        UUID pathId = pathOfQuiz(quizId);
        mutate(pathId, pathVersion);
        int updated = jdbc.update("update learning_question set prompt = ? where id = ?", required(prompt, "prompt", 500), questionId);
        if (updated == 0) {
            throw missing();
        }
        jdbc.update("update learning_quiz set content_version = content_version + 1, version = version + 1 where id = ?", quizId);
        audit.record(authorization.requireAccess().userId(), AuditEventType.LESSON_UPDATED, "learning_question", questionId.toString(), Map.of("scope", "learning"));
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> submit(UUID pathId, long version) {
        return transition(pathId, version, EditorialWorkflow::submit, AuditEventType.LESSON_SUBMITTED, true, false);
    }

    @Transactional
    public Map<String, Object> requestChanges(UUID pathId, long version) {
        return transition(pathId, version, EditorialWorkflow::requestChanges, AuditEventType.LESSON_CHANGES_REQUESTED, false, false);
    }

    @Transactional
    public Map<String, Object> verify(UUID pathId, long version) {
        return transition(pathId, version, EditorialWorkflow::verify, AuditEventType.LESSON_VERIFIED, false, true);
    }

    @Transactional
    public Map<String, Object> publish(UUID pathId, long version) {
        UUID actor = authorization.requireAccess().userId();
        PathRow path = lock(pathId);
        if (path.version != version) {
            throw conflict();
        }
        EditorialGuards.requireDifferentPerson(path.verifiedBy, actor, "لا ينشر المراجع المادة التي راجعها.");
        PublicationStatus next = EditorialWorkflow.publish(path.status);
        validatePublishable(pathId);
        String previous = path.snapshot;
        String snapshot = writeSnapshot(pathId, path.revision);
        Instant now = time.now();
        jdbc.update("""
                update learning_path set status = ?, published_snapshot = cast(? as jsonb), published_by = ?, updated_by = ?, updated_at = ?, version = version + 1
                where id = ? and version = ?
                """, next.name(), snapshot, actor, actor, timestamp(now), pathId, version);
        remember(pathId, path.revision, next.name(), snapshot, actor, now);
        search.sync(pathId, previous, now);
        auditLessons(actor, AuditEventType.LESSON_PUBLISHED, pathId);
        invalidate();
        return adminPath(pathId);
    }

    @Transactional
    public Map<String, Object> archive(UUID pathId, long version) {
        UUID actor = authorization.requireAccess().userId();
        PathRow path = lock(pathId);
        if (path.version != version) {
            throw conflict();
        }
        PublicationStatus next = EditorialWorkflow.archive(path.status);
        Instant now = time.now();
        if (path.snapshot != null) {
            remember(pathId, path.revision, next.name(), path.snapshot, actor, now);
        }
        jdbc.update("update learning_path set status = ?, published_snapshot = null, updated_by = ?, updated_at = ?, version = version + 1 where id = ? and version = ?", next.name(), actor, timestamp(now), pathId, version);
        search.remove(pathId, path.snapshot);
        auditLessons(actor, AuditEventType.LESSON_ARCHIVED, pathId);
        invalidate();
        return adminPath(pathId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> adminPath(UUID pathId) {
        authorization.requireAccess();
        lock(pathId);
        return workingTree(pathId, true);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> adminPaths() {
        authorization.requireAccess();
        return jdbc.query("select id, title, slug, status, difficulty, display_order, version from learning_path order by display_order, title", (row, index) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.getObject("id", UUID.class));
            item.put("title", row.getString("title"));
            item.put("slug", row.getString("slug"));
            item.put("status", row.getString("status"));
            item.put("difficultyLabel", Difficulty.valueOf(row.getString("difficulty")).label());
            item.put("displayOrder", row.getInt("display_order"));
            item.put("version", row.getLong("version"));
            return item;
        });
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> publicPaths() {
        return cached("paths", () -> loadPublicPaths());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> publicPath(String slug) {
        return cached("path:" + slug, () -> {
            String raw = publicSnapshotBySlug(slug);
            if (raw == null) {
                throw missing();
            }
            return publicPathView(json.readTree(raw));
        });
    }

    @Transactional(readOnly = true)
    public Map<String, Object> publicLesson(String slug) {
        metrics.lessonView();
        return cached("lesson:" + slug, () -> findPublicLesson(slug));
    }

    @Transactional
    public Map<String, Object> startAttempt(UUID quizId, String client) {
        limits.acquire(client);
        LocatedQuiz located = locatePublicQuiz(quizId);
        if (located == null) {
            throw missing();
        }
        byte[] secret = new byte[32];
        RANDOM.nextBytes(secret);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
        Instant now = time.now();
        jdbc.update("""
                insert into learning_attempt (id, token_hash, quiz_id, quiz_version, snapshot, status, started_at, expires_at, version)
                values (?, ?, ?, ?, cast(? as jsonb), 'OPEN', ?, ?, 0)
                """, Ids.random(), sha(token), quizId, located.quiz.path("contentVersion").asInt(1), located.quiz.toString(), timestamp(now), timestamp(now.plus(properties.attemptTtl())));
        metrics.quizStarted();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("token", token);
        body.put("expiresAt", now.plus(properties.attemptTtl()).toString());
        body.put("title", located.quiz.path("title").asString());
        body.put("passingScore", located.quiz.path("passingScore").asInt());
        body.put("questions", publicQuestions(located.quiz));
        return body;
    }

    @Transactional
    public Map<String, Object> submitAttempt(String token, String idempotencyKey, List<AnswerInput> answers, String client) {
        limits.acquire(client);
        if (token == null || token.isBlank() || token.length() > 80) {
            throw missing();
        }
        String hash = sha(token);
        AttemptRow attempt = jdbc.query("""
                select quiz_version, snapshot::text, status, idempotency_key, result::text, expires_at, version
                from learning_attempt where token_hash = ? for update
                """, (row, index) -> new AttemptRow(row.getInt(1), row.getString(2), row.getString(3), row.getString(4), row.getString(5), row.getTimestamp(6).toInstant(), row.getLong(7)), hash)
                .stream().findFirst().orElseThrow(this::missing);
        String key = idempotencyKey == null || idempotencyKey.isBlank() ? sha(canonical(answers)) : required(idempotencyKey, "idempotencyKey", 80);
        if ("SUBMITTED".equals(attempt.status)) {
            if (key.equals(attempt.idempotencyKey)) {
                return json.treeToValue(json.readTree(attempt.result), Map.class);
            }
            throw new ConflictException("أُرسلت هذه المحاولة من قبل.");
        }
        if (time.now().isAfter(attempt.expiresAt)) {
            throw invalid("token", "انتهت صلاحية المحاولة.");
        }
        JsonNode snapshot = json.readTree(attempt.snapshot);
        Map<String, Object> result = score(snapshot, answers);
        int updated = jdbc.update("""
                update learning_attempt set status = 'SUBMITTED', idempotency_key = ?, score = ?, passed = ?, result = cast(? as jsonb), submitted_at = ?, version = version + 1
                where token_hash = ? and status = 'OPEN' and version = ?
                """, key, result.get("score"), result.get("passed"), json.writeValueAsString(result), timestamp(time.now()), hash, attempt.version);
        if (updated == 0) {
            throw conflict();
        }
        metrics.quizSubmitted(Boolean.TRUE.equals(result.get("passed")));
        return result;
    }

    private Map<String, Object> transition(UUID pathId, long version, java.util.function.UnaryOperator<PublicationStatus> move, AuditEventType event, boolean recordSubmitter, boolean recordReviewer) {
        UUID actor = authorization.requireAccess().userId();
        PathRow path = lock(pathId);
        if (path.version != version) {
            throw conflict();
        }
        if (recordReviewer) {
            EditorialGuards.requireDifferentPerson(path.createdBy, actor, "لا يراجع منشئ المحتوى عمله.");
            EditorialGuards.requireDifferentPerson(path.submittedBy, actor, "لا يراجع من أرسل المحتوى عمله.");
        }
        PublicationStatus next = move.apply(path.status);
        Instant now = time.now();
        jdbc.update("""
                update learning_path set status = ?, updated_by = ?, updated_at = ?, version = version + 1,
                    submitted_by = case when ? then ? else submitted_by end,
                    verified_by = case when ? then ? else verified_by end
                where id = ? and version = ?
                """, next.name(), actor, timestamp(now), recordSubmitter, actor, recordReviewer, actor, pathId, version);
        auditLessons(actor, event, pathId);
        return adminPath(pathId);
    }

    private UUID mutate(UUID pathId, long version) {
        UUID actor = authorization.requireAccess().userId();
        int updated = jdbc.update("""
                update learning_path
                set status = case when status = 'PUBLISHED' then 'DRAFT' else status end,
                    revision = case when status = 'PUBLISHED' then revision + 1 else revision end,
                    submitted_by = case when status = 'PUBLISHED' then null else submitted_by end,
                    verified_by = case when status = 'PUBLISHED' then null else verified_by end,
                    version = version + 1, updated_by = ?, updated_at = ?
                where id = ? and version = ? and status in ('DRAFT', 'CHANGES_REQUESTED', 'PUBLISHED')
                """, actor, timestamp(time.now()), pathId, version);
        if (updated == 0) {
            throw conflict();
        }
        return actor;
    }

    private void validatePublishable(UUID pathId) {
        List<Integer> unitOrders = jdbc.query("select display_order from learning_unit where path_id = ? order by display_order", (row, index) -> row.getInt(1), pathId);
        requireOrders(unitOrders, "أضف وحدة واحدة على الأقل بترتيب متصل.");
        List<UUID> lessons = new ArrayList<>();
        for (UUID unitId : jdbc.query("select id from learning_unit where path_id = ?", (row, index) -> row.getObject(1, UUID.class), pathId)) {
            List<UUID> unitLessons = jdbc.query("select id from learning_lesson where unit_id = ? order by display_order", (row, index) -> row.getObject(1, UUID.class), unitId);
            requireOrders(jdbc.query("select display_order from learning_lesson where unit_id = ?", (row, index) -> row.getInt(1), unitId), "رتّب دروس الوحدة بلا فراغات.");
            lessons.addAll(unitLessons);
        }
        if (lessons.isEmpty()) {
            throw invalid("lessons", "المسار يحتاج درسًا واحدًا على الأقل.");
        }
        for (UUID lessonId : lessons) {
            if (jdbc.queryForObject("select count(*) from learning_objective where lesson_id = ?", Integer.class, lessonId) == 0) {
                throw invalid("objectives", "كل درس يحتاج هدفًا واحدًا على الأقل.");
            }
            if (jdbc.queryForObject("select count(*) from learning_section where lesson_id = ?", Integer.class, lessonId) == 0) {
                throw invalid("sections", "كل درس يحتاج قسمًا واحدًا على الأقل.");
            }
            Integer quotations = jdbc.queryForObject("select count(*) from learning_section where lesson_id = ? and example_kind = 'QUOTATION'", Integer.class, lessonId);
            Integer refs = jdbc.queryForObject("select count(*) from learning_reference where lesson_id = ?", Integer.class, lessonId);
            if (quotations != null && quotations > 0 && (refs == null || refs == 0)) {
                throw invalid("references", "المثال المقتبس يحتاج مرجعًا منشورًا.");
            }
            requireOrders(jdbc.query("select display_order from learning_objective where lesson_id = ?", (row, index) -> row.getInt(1), lessonId), "رتّب أهداف الدرس بلا فراغات.");
            requireOrders(jdbc.query("select display_order from learning_section where lesson_id = ?", (row, index) -> row.getInt(1), lessonId), "رتّب أقسام الدرس بلا فراغات.");
        }
        List<UUID> quizzes = jdbc.query("select id from learning_quiz where lesson_id in (select id from learning_lesson where unit_id in (select id from learning_unit where path_id = ?))", (row, index) -> row.getObject(1, UUID.class), pathId);
        for (UUID quizId : quizzes) {
            List<UUID> questions = jdbc.query("select id from learning_question where quiz_id = ? order by display_order", (row, index) -> row.getObject(1, UUID.class), quizId);
            requireOrders(jdbc.query("select display_order from learning_question where quiz_id = ?", (row, index) -> row.getInt(1), quizId), "رتّب أسئلة الاختبار بلا فراغات.");
            if (questions.isEmpty()) {
                throw invalid("questions", "الاختبار يحتاج سؤالًا واحدًا على الأقل.");
            }
        }
    }

    private String writeSnapshot(UUID pathId, int revision) {
        Map<String, Object> row = jdbc.queryForMap("select title, slug, summary, description, difficulty, estimated_minutes, display_order, prerequisite_id from learning_path where id = ?", pathId);
        ObjectNode root = json.createObjectNode();
        root.put("revision", revision);
        root.put("title", String.valueOf(row.get("title")));
        root.put("slug", String.valueOf(row.get("slug")));
        root.put("summary", String.valueOf(row.get("summary")));
        root.put("description", String.valueOf(row.get("description")));
        root.put("difficultyLabel", Difficulty.valueOf(String.valueOf(row.get("difficulty"))).label());
        if (row.get("estimated_minutes") != null) {
            root.put("estimatedMinutes", ((Number) row.get("estimated_minutes")).intValue());
        }
        root.put("displayOrder", ((Number) row.get("display_order")).intValue());
        if (row.get("prerequisite_id") != null) {
            root.put("prerequisiteId", row.get("prerequisite_id").toString());
        }
        ArrayNode units = root.putArray("units");
        List<Map<String, Object>> unitRows = jdbc.queryForList("select id, title, summary, display_order from learning_unit where path_id = ? order by display_order", pathId);
        for (Map<String, Object> unit : unitRows) {
            ObjectNode node = units.addObject();
            UUID unitId = (UUID) unit.get("id");
            node.put("id", unitId.toString());
            node.put("title", String.valueOf(unit.get("title")));
            node.put("summary", String.valueOf(unit.get("summary")));
            node.put("displayOrder", ((Number) unit.get("display_order")).intValue());
            ArrayNode lessons = node.putArray("lessons");
            for (Map<String, Object> lesson : jdbc.queryForList("select id, title, slug, summary, estimated_minutes, display_order from learning_lesson where unit_id = ? order by display_order", unitId)) {
                ObjectNode lessonNode = lessons.addObject();
                UUID lessonId = (UUID) lesson.get("id");
                lessonNode.put("id", lessonId.toString());
                lessonNode.put("title", String.valueOf(lesson.get("title")));
                lessonNode.put("slug", String.valueOf(lesson.get("slug")));
                lessonNode.put("summary", String.valueOf(lesson.get("summary")));
                if (lesson.get("estimated_minutes") != null) {
                    lessonNode.put("estimatedMinutes", ((Number) lesson.get("estimated_minutes")).intValue());
                }
                lessonNode.put("displayOrder", ((Number) lesson.get("display_order")).intValue());
                ArrayNode objectives = lessonNode.putArray("objectives");
                jdbc.queryForList("select statement from learning_objective where lesson_id = ? order by display_order", lessonId).forEach(objective -> objectives.add(String.valueOf(objective.get("statement"))));
                ArrayNode sections = lessonNode.putArray("sections");
                for (Map<String, Object> section : jdbc.queryForList("select section_type, heading, body, example_kind from learning_section where lesson_id = ? order by display_order", lessonId)) {
                    ObjectNode sectionNode = sections.addObject();
                    sectionNode.put("type", String.valueOf(section.get("section_type")));
                    sectionNode.put("typeLabel", SectionType.valueOf(String.valueOf(section.get("section_type"))).label());
                    sectionNode.put("heading", String.valueOf(section.get("heading")));
                    sectionNode.put("body", String.valueOf(section.get("body")));
                    sectionNode.put("exampleKind", String.valueOf(section.get("example_kind")));
                    if ("CONSTRUCTED".equals(section.get("example_kind"))) {
                        sectionNode.put("exampleLabel", "مثال تعليمي");
                    }
                }
                ArrayNode refs = lessonNode.putArray("references");
                for (Map<String, Object> reference : jdbc.queryForList("select target_kind, target_slug, note from learning_reference where lesson_id = ?", lessonId)) {
                    ObjectNode ref = refs.addObject();
                    ref.put("kind", String.valueOf(reference.get("target_kind")));
                    ref.put("kindLabel", ReferenceKind.valueOf(String.valueOf(reference.get("target_kind"))).label());
                    ref.put("slug", String.valueOf(reference.get("target_slug")));
                    if (reference.get("note") != null) {
                        ref.put("note", String.valueOf(reference.get("note")));
                    }
                }
                ArrayNode activities = lessonNode.putArray("activities");
                for (Map<String, Object> activity : jdbc.queryForList("select activity_type, title, instructions from learning_activity where lesson_id = ? order by display_order", lessonId)) {
                    ObjectNode activityNode = activities.addObject();
                    activityNode.put("type", String.valueOf(activity.get("activity_type")));
                    activityNode.put("typeLabel", ActivityType.valueOf(String.valueOf(activity.get("activity_type"))).label());
                    activityNode.put("title", String.valueOf(activity.get("title")));
                    activityNode.put("instructions", String.valueOf(activity.get("instructions")));
                }
                writeQuiz(lessonNode.putObject("quiz"), lessonId, null);
            }
            writeQuiz(node.putObject("unitQuiz"), null, unitId);
        }
        return root.toString();
    }

    private void writeQuiz(ObjectNode quizNode, UUID lessonId, UUID unitId) {
        List<Map<String, Object>> rows = lessonId != null
                ? jdbc.queryForList("select id, title, passing_score, max_attempts, content_version from learning_quiz where lesson_id = ?", lessonId)
                : jdbc.queryForList("select id, title, passing_score, max_attempts, content_version from learning_quiz where unit_id = ?", unitId);
        if (rows.isEmpty()) {
            return;
        }
        Map<String, Object> quiz = rows.get(0);
        UUID quizId = (UUID) quiz.get("id");
        quizNode.put("id", quizId.toString());
        quizNode.put("title", String.valueOf(quiz.get("title")));
        quizNode.put("passingScore", ((Number) quiz.get("passing_score")).intValue());
        quizNode.put("contentVersion", ((Number) quiz.get("content_version")).intValue());
        if (quiz.get("max_attempts") != null) {
            quizNode.put("maxAttempts", ((Number) quiz.get("max_attempts")).intValue());
        }
        ArrayNode questions = quizNode.putArray("questions");
        for (Map<String, Object> question : jdbc.queryForList("select id, prompt, explanation, question_type, knowledge_kind, knowledge_slug from learning_question where quiz_id = ? order by display_order", quizId)) {
            ObjectNode questionNode = questions.addObject();
            UUID questionId = (UUID) question.get("id");
            questionNode.put("id", questionId.toString());
            questionNode.put("prompt", String.valueOf(question.get("prompt")));
            questionNode.put("explanation", String.valueOf(question.get("explanation")));
            questionNode.put("type", String.valueOf(question.get("question_type")));
            if (question.get("knowledge_kind") != null) {
                questionNode.put("knowledgeKind", String.valueOf(question.get("knowledge_kind")));
                questionNode.put("knowledgeSlug", String.valueOf(question.get("knowledge_slug")));
            }
            ArrayNode options = questionNode.putArray("options");
            for (Map<String, Object> option : jdbc.queryForList("select id, label, correct from learning_question_option where question_id = ? order by display_order", questionId)) {
                ObjectNode optionNode = options.addObject();
                optionNode.put("id", option.get("id").toString());
                optionNode.put("label", String.valueOf(option.get("label")));
                optionNode.put("correct", Boolean.TRUE.equals(option.get("correct")));
            }
        }
    }

    private Map<String, Object> workingTree(UUID pathId, boolean includeAnswers) {
        Map<String, Object> path = new LinkedHashMap<>();
        jdbc.query("select id, title, slug, summary, description, difficulty, estimated_minutes, status, display_order, revision, version, prerequisite_id from learning_path where id = ?", row -> {
            path.put("id", row.getObject("id", UUID.class));
            path.put("title", row.getString("title"));
            path.put("slug", row.getString("slug"));
            path.put("summary", row.getString("summary"));
            path.put("description", row.getString("description"));
            path.put("difficultyLabel", Difficulty.valueOf(row.getString("difficulty")).label());
            path.put("status", row.getString("status"));
            path.put("displayOrder", row.getInt("display_order"));
            path.put("revision", row.getInt("revision"));
            path.put("version", row.getLong("version"));
            path.put("estimatedMinutes", row.getObject("estimated_minutes"));
            path.put("prerequisiteId", row.getObject("prerequisite_id", UUID.class));
        }, pathId);
        List<Map<String, Object>> units = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList("select id, title, summary, display_order, version from learning_unit where path_id = ? order by display_order", pathId)) {
            UUID unitId = (UUID) row.get("id");
            Map<String, Object> unit = new LinkedHashMap<>();
            unit.put("id", unitId);
            unit.put("title", row.get("title"));
            unit.put("summary", row.get("summary"));
            unit.put("displayOrder", row.get("display_order"));
            unit.put("version", row.get("version"));
            unit.put("lessons", lessons(unitId, includeAnswers));
            units.add(unit);
        }
        path.put("units", units);
        return path;
    }

    private List<Map<String, Object>> lessons(UUID unitId, boolean includeAnswers) {
        List<Map<String, Object>> lessons = new ArrayList<>();
        for (Map<String, Object> row : jdbc.queryForList("select id, title, slug, summary, estimated_minutes, display_order, version from learning_lesson where unit_id = ? order by display_order", unitId)) {
            UUID lessonId = (UUID) row.get("id");
            Map<String, Object> lesson = new LinkedHashMap<>();
            lesson.put("id", lessonId);
            lesson.put("title", row.get("title"));
            lesson.put("slug", row.get("slug"));
            lesson.put("summary", row.get("summary"));
            lesson.put("estimatedMinutes", row.get("estimated_minutes"));
            lesson.put("displayOrder", row.get("display_order"));
            lesson.put("version", row.get("version"));
            lesson.put("objectives", jdbc.queryForList("select statement, display_order from learning_objective where lesson_id = ? order by display_order", lessonId));
            lesson.put("sections", sections(lessonId));
            lesson.put("quiz", quizView(lessonId, includeAnswers));
            lessons.add(lesson);
        }
        return lessons;
    }

    private List<Map<String, Object>> sections(UUID lessonId) {
        List<Map<String, Object>> sections = new ArrayList<>();
        for (Map<String, Object> section : jdbc.queryForList("select section_type, heading, body, example_kind, display_order from learning_section where lesson_id = ? order by display_order", lessonId)) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("typeLabel", SectionType.valueOf(String.valueOf(section.get("section_type"))).label());
            item.put("heading", section.get("heading"));
            item.put("body", section.get("body"));
            item.put("exampleKind", section.get("example_kind"));
            if ("CONSTRUCTED".equals(section.get("example_kind"))) {
                item.put("exampleLabel", "مثال تعليمي");
            }
            item.put("displayOrder", section.get("display_order"));
            sections.add(item);
        }
        return sections;
    }

    private Map<String, Object> quizView(UUID lessonId, boolean includeAnswers) {
        List<Map<String, Object>> rows = jdbc.queryForList("select id, title, passing_score, content_version, version from learning_quiz where lesson_id = ?", lessonId);
        if (rows.isEmpty()) {
            return null;
        }
        Map<String, Object> quiz = new LinkedHashMap<>();
        UUID quizId = (UUID) rows.get(0).get("id");
        quiz.put("id", quizId);
        quiz.put("title", rows.get(0).get("title"));
        quiz.put("passingScore", rows.get(0).get("passing_score"));
        quiz.put("contentVersion", rows.get(0).get("content_version"));
        quiz.put("version", rows.get(0).get("version"));
        if (includeAnswers) {
            List<Map<String, Object>> questions = new ArrayList<>();
            for (Map<String, Object> row : jdbc.queryForList("select id, prompt, explanation, question_type, display_order from learning_question where quiz_id = ? order by display_order", quizId)) {
                UUID questionId = (UUID) row.get("id");
                Map<String, Object> question = new LinkedHashMap<>();
                question.put("id", questionId);
                question.put("prompt", row.get("prompt"));
                question.put("explanation", row.get("explanation"));
                question.put("type", row.get("question_type"));
                question.put("displayOrder", row.get("display_order"));
                question.put("options", jdbc.queryForList("select id, label, correct, display_order from learning_question_option where question_id = ? order by display_order", questionId));
                questions.add(question);
            }
            quiz.put("questions", questions);
        }
        return quiz;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> loadPublicPaths() {
        List<Map<String, Object>> paths = new ArrayList<>();
        jdbc.query("select published_snapshot::text from learning_path where published_snapshot is not null and status <> 'ARCHIVED' order by display_order", row -> {
            JsonNode snapshot = json.readTree(row.getString(1));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("title", snapshot.path("title").asString());
            item.put("slug", snapshot.path("slug").asString());
            item.put("summary", snapshot.path("summary").asString());
            item.put("difficultyLabel", snapshot.path("difficultyLabel").asString());
            item.put("estimatedMinutes", snapshot.path("estimatedMinutes").isMissingNode() ? null : snapshot.path("estimatedMinutes").asInt());
            item.put("displayOrder", snapshot.path("displayOrder").asInt());
            paths.add(item);
        });
        paths.sort((left, right) -> Integer.compare((Integer) left.get("displayOrder"), (Integer) right.get("displayOrder")));
        return paths;
    }

    private Map<String, Object> publicPathView(JsonNode snapshot) {
        Map<String, Object> path = new LinkedHashMap<>();
        path.put("title", snapshot.path("title").asString());
        path.put("slug", snapshot.path("slug").asString());
        path.put("summary", snapshot.path("summary").asString());
        path.put("description", snapshot.path("description").asString());
        path.put("difficultyLabel", snapshot.path("difficultyLabel").asString());
        path.put("estimatedMinutes", snapshot.path("estimatedMinutes").isMissingNode() ? null : snapshot.path("estimatedMinutes").asInt());
        if (!snapshot.path("prerequisiteId").isMissingNode()) {
            String raw = publicSnapshotById(UUID.fromString(snapshot.path("prerequisiteId").asString()));
            if (raw != null) {
                JsonNode prerequisite = json.readTree(raw);
                path.put("prerequisite", Map.of("title", prerequisite.path("title").asString(), "slug", prerequisite.path("slug").asString()));
            }
        }
        List<Map<String, Object>> units = new ArrayList<>();
        for (JsonNode unit : snapshot.path("units")) {
            List<Map<String, Object>> lessons = new ArrayList<>();
            for (JsonNode lesson : unit.path("lessons")) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("title", lesson.path("title").asString());
                item.put("slug", lesson.path("slug").asString());
                item.put("summary", lesson.path("summary").asString());
                item.put("estimatedMinutes", lesson.path("estimatedMinutes").isMissingNode() ? null : lesson.path("estimatedMinutes").asInt());
                lessons.add(item);
            }
            units.add(Map.of("title", unit.path("title").asString(), "summary", unit.path("summary").asString(), "lessons", lessons));
        }
        path.put("units", units);
        return path;
    }

    private Map<String, Object> findPublicLesson(String slug) {
        List<String> snapshots = jdbc.query("select published_snapshot::text from learning_path where published_snapshot is not null and status <> 'ARCHIVED'", (row, index) -> row.getString(1));
        for (String raw : snapshots) {
            JsonNode snapshot = json.readTree(raw);
            List<JsonNode> lessons = new ArrayList<>();
            for (JsonNode unit : snapshot.path("units")) {
                for (JsonNode lesson : unit.path("lessons")) {
                    lessons.add(lesson);
                }
            }
            for (int index = 0; index < lessons.size(); index++) {
                if (!slug.equals(lessons.get(index).path("slug").asString())) {
                    continue;
                }
                return publicLessonView(snapshot, lessons, index);
            }
        }
        throw missing();
    }

    private Map<String, Object> publicLessonView(JsonNode path, List<JsonNode> lessons, int index) {
        JsonNode lesson = lessons.get(index);
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("title", lesson.path("title").asString());
        view.put("slug", lesson.path("slug").asString());
        view.put("summary", lesson.path("summary").asString());
        view.put("pathTitle", path.path("title").asString());
        view.put("pathSlug", path.path("slug").asString());
        view.put("estimatedMinutes", lesson.path("estimatedMinutes").isMissingNode() ? null : lesson.path("estimatedMinutes").asInt());
        List<String> objectives = new ArrayList<>();
        lesson.path("objectives").forEach(item -> objectives.add(item.asString()));
        view.put("objectives", objectives);
        List<Map<String, Object>> sections = new ArrayList<>();
        for (JsonNode section : lesson.path("sections")) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("typeLabel", section.path("typeLabel").asString());
            item.put("heading", section.path("heading").asString());
            item.put("body", section.path("body").asString());
            if (!section.path("exampleLabel").isMissingNode()) {
                item.put("exampleLabel", section.path("exampleLabel").asString());
            }
            sections.add(item);
        }
        view.put("sections", sections);
        List<Map<String, Object>> refs = new ArrayList<>();
        for (JsonNode reference : lesson.path("references")) {
            ReferenceKind kind = ReferenceKind.valueOf(reference.path("kind").asString());
            var resolved = references.resolve(kind, reference.path("slug").asString());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("kindLabel", reference.path("kindLabel").asString());
            item.put("title", resolved.map(PublishedReferencePort.ResolvedReference::title).orElse("لم يعد هذا المرجع منشورًا"));
            item.put("href", resolved.map(PublishedReferencePort.ResolvedReference::href).orElse(null));
            item.put("note", reference.path("note").isMissingNode() ? null : reference.path("note").asString());
            item.put("available", resolved.isPresent());
            refs.add(item);
        }
        view.put("references", refs);
        List<Map<String, Object>> activities = new ArrayList<>();
        for (JsonNode activity : lesson.path("activities")) {
            activities.add(Map.of("typeLabel", activity.path("typeLabel").asString(), "title", activity.path("title").asString(), "instructions", activity.path("instructions").asString()));
        }
        view.put("activities", activities);
        if (index > 0) {
            view.put("previous", Map.of("title", lessons.get(index - 1).path("title").asString(), "slug", lessons.get(index - 1).path("slug").asString()));
        }
        if (index + 1 < lessons.size()) {
            view.put("next", Map.of("title", lessons.get(index + 1).path("title").asString(), "slug", lessons.get(index + 1).path("slug").asString()));
        }
        JsonNode quiz = lesson.path("quiz");
        if (quiz.hasNonNull("id")) {
            Map<String, Object> quizView = new LinkedHashMap<>();
            quizView.put("id", quiz.path("id").asString());
            quizView.put("title", quiz.path("title").asString());
            quizView.put("passingScore", quiz.path("passingScore").asInt());
            quizView.put("questionCount", quiz.path("questions").size());
            view.put("quiz", quizView);
        }
        return view;
    }

    private LocatedQuiz locatePublicQuiz(UUID quizId) {
        List<String> snapshots = jdbc.query("select published_snapshot::text from learning_path where published_snapshot is not null and status <> 'ARCHIVED'", (row, index) -> row.getString(1));
        for (String raw : snapshots) {
            JsonNode snapshot = json.readTree(raw);
            for (JsonNode unit : snapshot.path("units")) {
                LocatedQuiz found = quizNode(unit.path("unitQuiz"), quizId);
                if (found != null) {
                    return found;
                }
                for (JsonNode lesson : unit.path("lessons")) {
                    found = quizNode(lesson.path("quiz"), quizId);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    private static LocatedQuiz quizNode(JsonNode quiz, UUID quizId) {
        if (quiz == null || !quiz.hasNonNull("id") || !quizId.toString().equals(quiz.path("id").asString())) {
            return null;
        }
        return new LocatedQuiz(quiz);
    }

    private List<Map<String, Object>> publicQuestions(JsonNode quiz) {
        List<Map<String, Object>> questions = new ArrayList<>();
        for (JsonNode question : quiz.path("questions")) {
            List<Map<String, Object>> options = new ArrayList<>();
            for (JsonNode option : question.path("options")) {
                options.add(Map.of("id", option.path("id").asString(), "label", option.path("label").asString()));
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", question.path("id").asString());
            item.put("prompt", question.path("prompt").asString());
            item.put("type", question.path("type").asString());
            item.put("options", options);
            questions.add(item);
        }
        return questions;
    }

    private Map<String, Object> score(JsonNode snapshot, List<AnswerInput> answers) {
        List<JsonNode> questions = new ArrayList<>();
        snapshot.path("questions").forEach(questions::add);
        if (answers.size() != questions.size()) {
            throw invalid("answers", "الإجابة لا تطابق أسئلة هذه المحاولة.");
        }
        List<QuizScorer.Key> keys = new ArrayList<>();
        List<List<UUID>> selected = new ArrayList<>();
        List<Map<String, Object>> details = new ArrayList<>();
        for (JsonNode question : questions) {
            AnswerInput answer = answers.stream().filter(item -> question.path("id").asString().equals(item.questionId().toString())).findFirst().orElseThrow(() -> invalid("answers", "الإجابة لا تطابق أسئلة هذه المحاولة."));
            if (answer.optionIds().size() != answer.optionIds().stream().distinct().count()) {
                throw invalid("options", "تكرر خيار في الإجابة.");
            }
            List<String> knownOptions = new ArrayList<>();
            List<UUID> correct = new ArrayList<>();
            for (JsonNode option : question.path("options")) {
                knownOptions.add(option.path("id").asString());
                if (option.path("correct").asBoolean()) {
                    correct.add(UUID.fromString(option.path("id").asString()));
                }
            }
            if (answer.optionIds().stream().anyMatch(id -> !knownOptions.contains(id.toString()))) {
                throw invalid("options", "الإجابة لا تطابق أسئلة هذه المحاولة.");
            }
            keys.add(new QuizScorer.Key(answer.questionId(), QuestionType.valueOf(question.path("type").asString()), new java.util.HashSet<>(correct)));
            selected.add(answer.optionIds());
        }
        QuizScorer.Outcome outcome = QuizScorer.score(snapshot.path("passingScore").asInt(), keys, selected);
        for (int index = 0; index < questions.size(); index++) {
            JsonNode question = questions.get(index);
            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("questionId", question.path("id").asString());
            detail.put("accepted", QuizScorer.matches(keys.get(index), selected.get(index)));
            detail.put("explanation", question.path("explanation").asString());
            if (question.hasNonNull("knowledgeKind")) {
                var resolved = references.resolve(ReferenceKind.valueOf(question.path("knowledgeKind").asString()), question.path("knowledgeSlug").asString());
                detail.put("referenceTitle", resolved.map(PublishedReferencePort.ResolvedReference::title).orElse(null));
                detail.put("referenceHref", resolved.map(PublishedReferencePort.ResolvedReference::href).orElse(null));
            }
            details.add(detail);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", outcome.score());
        result.put("passed", outcome.passed());
        result.put("questions", details);
        return result;
    }

    private String publicSnapshotBySlug(String slug) {
        return jdbc.query("select published_snapshot::text from learning_path where slug = ? and published_snapshot is not null and status <> 'ARCHIVED'", (row, index) -> row.getString(1), slug).stream().findFirst().orElse(null);
    }

    private String publicSnapshotById(UUID id) {
        return jdbc.query("select published_snapshot::text from learning_path where id = ? and published_snapshot is not null and status <> 'ARCHIVED'", (row, index) -> row.getString(1), id).stream().findFirst().orElse(null);
    }

    private void remember(UUID pathId, int revision, String status, String snapshot, UUID actor, Instant now) {
        jdbc.update("insert into learning_revision (id, path_id, revision, status, snapshot, created_by, created_at) values (?, ?, ?, ?, cast(? as jsonb), ?, ?)", Ids.random(), pathId, revision, status, snapshot, actor, timestamp(now));
    }

    private void auditLessons(UUID actor, AuditEventType event, UUID pathId) {
        List<UUID> lessons = jdbc.query("select id from learning_lesson where unit_id in (select id from learning_unit where path_id = ?)", (row, index) -> row.getObject(1, UUID.class), pathId);
        if (lessons.isEmpty()) {
            audit.record(actor, event, "learning_path", pathId.toString(), Map.of("scope", "learning"));
            return;
        }
        for (UUID lessonId : lessons) {
            audit.record(actor, event, "learning_lesson", lessonId.toString(), Map.of("path", pathId.toString()));
        }
    }

    private PathRow lock(UUID id) {
        return jdbc.query("""
                select status, version, revision, created_by, submitted_by, verified_by, published_snapshot::text
                from learning_path where id = ? for update
                """, (row, index) -> new PathRow(PublicationStatus.valueOf(row.getString(1)), row.getLong(2), row.getInt(3), row.getObject(4, UUID.class), row.getObject(5, UUID.class), row.getObject(6, UUID.class), row.getString(7)), id)
                .stream().findFirst().orElseThrow(this::missing);
    }

    private UUID pathOfUnit(UUID unitId) {
        return jdbc.query("select path_id from learning_unit where id = ?", (row, index) -> row.getObject(1, UUID.class), unitId).stream().findFirst().orElseThrow(this::missing);
    }

    private UUID pathOfLesson(UUID lessonId) {
        return jdbc.query("select path_id from learning_unit where id = (select unit_id from learning_lesson where id = ?)", (row, index) -> row.getObject(1, UUID.class), lessonId).stream().findFirst().orElseThrow(this::missing);
    }

    private UUID pathOfQuiz(UUID quizId) {
        return jdbc.query("""
                select coalesce(
                    (select u.path_id from learning_quiz q join learning_lesson l on l.id = q.lesson_id join learning_unit u on u.id = l.unit_id where q.id = ?),
                    (select u.path_id from learning_quiz q join learning_unit u on u.id = q.unit_id where q.id = ?)
                )
                """, (row, index) -> row.getObject(1, UUID.class), quizId, quizId).stream().findFirst().orElseThrow(this::missing);
    }

    private void insert(String sql, Object... args) {
        try {
            jdbc.update(sql, args);
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("تعارض في ترتيب المحتوى أو تكرار المرجع.");
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T cached(String key, java.util.function.Supplier<T> loader) {
        synchronized (cacheLock) {
            String versioned = cacheGeneration + ":" + key;
            Object hit = cache.get(versioned);
            if (hit != null) {
                return (T) hit;
            }
            T loaded = loader.get();
            cache.put(versioned, loaded);
            return loaded;
        }
    }

    private void invalidate() {
        synchronized (cacheLock) {
            cacheGeneration++;
            cache.clear();
        }
    }

    private static Timestamp timestamp(Instant instant) {
        return Timestamp.from(instant);
    }

    private static String canonical(List<AnswerInput> answers) {
        StringBuilder builder = new StringBuilder();
        for (AnswerInput answer : answers) {
            builder.append(answer.questionId()).append(':');
            answer.optionIds().stream().sorted().forEach(id -> builder.append(id).append(','));
            builder.append(';');
        }
        return builder.toString();
    }

    private static String sha(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static void requireOrders(List<Integer> orders, String message) {
        try {
            DisplayOrder.requireContiguous(orders);
        } catch (IllegalArgumentException exception) {
            throw invalid("displayOrder", message);
        }
    }

    private static Difficulty difficulty(String raw) {
        try {
            return Difficulty.parse(raw);
        } catch (RuntimeException exception) {
            throw invalid("difficulty", "المستوى مبتدئ أو متوسط أو متقدم.");
        }
    }

    private static <T extends Enum<T>> T enumValue(Class<T> type, String raw, String field) {
        try {
            return Enum.valueOf(type, raw);
        } catch (RuntimeException exception) {
            throw invalid(field, "القيمة غير مدعومة.");
        }
    }

    private static String required(String value, String field, int max) {
        if (value == null || value.isBlank() || value.codePointCount(0, value.length()) > max) {
            throw invalid(field, "النص مطلوب وضمن الحد المسموح.");
        }
        return value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }

    private static ConflictException conflict() {
        return new ConflictException("تعارض تعديل. أعد تحميل المحتوى.");
    }

    private ResourceNotFoundException missing() {
        return new ResourceNotFoundException("المحتوى التعليمي غير موجود.");
    }

    public record OptionInput(String label, boolean correct) {
    }

    public record AnswerInput(UUID questionId, List<UUID> optionIds) {
    }

    private record PathRow(PublicationStatus status, long version, int revision, UUID createdBy, UUID submittedBy, UUID verifiedBy, String snapshot) {
    }

    private record AttemptRow(int quizVersion, String snapshot, String status, String idempotencyKey, String result, Instant expiresAt, long version) {
    }

    private record LocatedQuiz(JsonNode quiz) {
    }
}
