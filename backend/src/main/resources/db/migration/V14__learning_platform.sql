ALTER TABLE admin_audit_event DROP CONSTRAINT ck_admin_audit_event_type;
ALTER TABLE admin_audit_event ADD CONSTRAINT ck_admin_audit_event_type CHECK (event_type IN (
    'ADMIN_LOGIN_SUCCEEDED',
    'ADMIN_LOGIN_FAILED',
    'ADMIN_LOGOUT',
    'ADMIN_PASSWORD_CHANGED',
    'ADMIN_USER_CREATED',
    'ADMIN_USER_UPDATED',
    'ADMIN_USER_ACTIVATED',
    'ADMIN_USER_DEACTIVATED',
    'ADMIN_USER_UNLOCKED',
    'ADMIN_PASSWORD_RESET',
    'ADMIN_ROLE_CREATED',
    'ADMIN_ROLE_UPDATED',
    'ADMIN_ROLE_ASSIGNED',
    'ADMIN_ROLE_REMOVED',
    'ROOT_CREATED',
    'DICTIONARY_ENTRY_CREATED',
    'DICTIONARY_ENTRY_UPDATED',
    'SENSE_ADDED',
    'FORM_ADDED',
    'RELATION_ADDED',
    'SOURCE_CREATED',
    'CITATION_ADDED',
    'CONTENT_SUBMITTED',
    'CONTENT_CHANGES_REQUESTED',
    'CONTENT_VERIFIED',
    'CONTENT_PUBLISHED',
    'CONTENT_ARCHIVED',
    'MORPHOLOGY_PATTERN_CREATED',
    'MORPHOLOGY_PATTERN_UPDATED',
    'MORPHOLOGY_ANALYSIS_CREATED',
    'MORPHOLOGY_ANALYSIS_UPDATED',
    'MORPHOLOGY_ANALYSIS_VERIFIED',
    'MORPHOLOGY_ANALYSIS_PUBLISHED',
    'MORPHOLOGY_RULE_CHANGED',
    'GRAMMAR_TOPIC_CREATED',
    'GRAMMAR_TOPIC_UPDATED',
    'GRAMMAR_RULE_CREATED',
    'GRAMMAR_RULE_UPDATED',
    'GRAMMAR_CONCEPT_CREATED',
    'GRAMMAR_EXAMPLE_ADDED',
    'GRAMMAR_ANNOTATION_CREATED',
    'GRAMMAR_CONTENT_SUBMITTED',
    'GRAMMAR_CONTENT_CHANGES_REQUESTED',
    'GRAMMAR_CONTENT_VERIFIED',
    'GRAMMAR_CONTENT_PUBLISHED',
    'GRAMMAR_CONTENT_ARCHIVED',
    'SEARCH_INDEX_REBUILT',
    'SEARCH_INDEX_REPAIR',
    'SPELLING_TOPIC_CREATED',
    'SPELLING_TOPIC_UPDATED',
    'SPELLING_TOPIC_PUBLISHED',
    'SPELLING_RULE_CREATED',
    'SPELLING_RULE_UPDATED',
    'SPELLING_RULE_PUBLISHED',
    'SPELLING_CONTENT_SUBMITTED',
    'SPELLING_CONTENT_CHANGES_REQUESTED',
    'SPELLING_CONTENT_VERIFIED',
    'SPELLING_CONTENT_ARCHIVED',
    'RHETORIC_TOPIC_CREATED',
    'RHETORIC_TOPIC_PUBLISHED',
    'RHETORIC_DEVICE_CREATED',
    'RHETORIC_DEVICE_UPDATED',
    'RHETORIC_DEVICE_PUBLISHED',
    'RHETORIC_CONTENT_SUBMITTED',
    'RHETORIC_CONTENT_CHANGES_REQUESTED',
    'RHETORIC_CONTENT_VERIFIED',
    'RHETORIC_CONTENT_ARCHIVED',
    'LITERARY_ERA_CREATED',
    'LITERARY_ERA_UPDATED',
    'LITERARY_ERA_PUBLISHED',
    'LITERARY_GENRE_PUBLISHED',
    'LITERARY_SCHOOL_PUBLISHED',
    'LITERARY_FIGURE_CREATED',
    'LITERARY_FIGURE_UPDATED',
    'LITERARY_FIGURE_PUBLISHED',
    'LITERARY_WORK_CREATED',
    'LITERARY_WORK_UPDATED',
    'LITERARY_WORK_PUBLISHED',
    'LITERARY_CONTENT_SUBMITTED',
    'LITERARY_CONTENT_CHANGES_REQUESTED',
    'LITERARY_CONTENT_VERIFIED',
    'LITERARY_CONTENT_ARCHIVED',
    'ARTICLE_CREATED',
    'ARTICLE_UPDATED',
    'ARTICLE_SUBMITTED',
    'ARTICLE_CHANGES_REQUESTED',
    'ARTICLE_VERIFIED',
    'ARTICLE_PUBLISHED',
    'ARTICLE_ARCHIVED',
    'LEARNING_PATH_CREATED',
    'LEARNING_PATH_UPDATED',
    'LESSON_CREATED',
    'LESSON_UPDATED',
    'LESSON_SUBMITTED',
    'LESSON_CHANGES_REQUESTED',
    'LESSON_VERIFIED',
    'LESSON_PUBLISHED',
    'LESSON_ARCHIVED',
    'QUIZ_CREATED',
    'QUESTION_CREATED'
));

ALTER TABLE search_document DROP CONSTRAINT ck_search_document_type;
ALTER TABLE search_document ADD CONSTRAINT ck_search_document_type CHECK (entity_type IN (
    'DICTIONARY_ENTRY',
    'ROOT',
    'GRAMMAR_TOPIC',
    'GRAMMAR_RULE',
    'GRAMMAR_CONCEPT',
    'SPELLING_TOPIC',
    'SPELLING_RULE',
    'RHETORIC_TOPIC',
    'RHETORIC_DEVICE',
    'LITERARY_ERA',
    'LITERARY_FIGURE',
    'LITERARY_WORK',
    'ARTICLE',
    'LEARNING_PATH',
    'LESSON'
));

UPDATE search_index_state SET index_version = 3 WHERE id = 1;

CREATE TABLE learning_path (
    id                  UUID PRIMARY KEY,
    title               VARCHAR(160)  NOT NULL,
    slug                VARCHAR(180)  NOT NULL UNIQUE,
    summary             VARCHAR(500)  NOT NULL,
    description         TEXT          NOT NULL,
    difficulty          VARCHAR(20)   NOT NULL,
    estimated_minutes   INTEGER,
    status              VARCHAR(32)   NOT NULL,
    display_order       INTEGER       NOT NULL,
    revision            INTEGER       NOT NULL,
    version             BIGINT        NOT NULL,
    prerequisite_id     UUID,
    created_by          UUID          NOT NULL,
    updated_by          UUID          NOT NULL,
    submitted_by        UUID,
    verified_by         UUID,
    published_by        UUID,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    published_snapshot  JSONB,
    CONSTRAINT fk_learning_path_prerequisite FOREIGN KEY (prerequisite_id) REFERENCES learning_path (id),
    CONSTRAINT ck_learning_path_difficulty CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT ck_learning_path_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT ck_learning_path_minutes CHECK (estimated_minutes IS NULL OR estimated_minutes BETWEEN 1 AND 600),
    CONSTRAINT ck_learning_path_order CHECK (display_order >= 1)
);

CREATE TABLE learning_unit (
    id              UUID PRIMARY KEY,
    path_id         UUID          NOT NULL REFERENCES learning_path (id),
    title           VARCHAR(160)  NOT NULL,
    summary         VARCHAR(500)  NOT NULL,
    display_order   INTEGER       NOT NULL,
    version         BIGINT        NOT NULL,
    CONSTRAINT uq_learning_unit_order UNIQUE (path_id, display_order),
    CONSTRAINT ck_learning_unit_order CHECK (display_order >= 1)
);

CREATE TABLE learning_lesson (
    id                  UUID PRIMARY KEY,
    unit_id             UUID          NOT NULL REFERENCES learning_unit (id),
    title               VARCHAR(160)  NOT NULL,
    slug                VARCHAR(180)  NOT NULL UNIQUE,
    summary             VARCHAR(500)  NOT NULL,
    estimated_minutes   INTEGER,
    display_order       INTEGER       NOT NULL,
    version             BIGINT        NOT NULL,
    CONSTRAINT uq_learning_lesson_order UNIQUE (unit_id, display_order),
    CONSTRAINT ck_learning_lesson_order CHECK (display_order >= 1),
    CONSTRAINT ck_learning_lesson_minutes CHECK (estimated_minutes IS NULL OR estimated_minutes BETWEEN 1 AND 180)
);

CREATE INDEX ix_learning_lesson_unit ON learning_lesson (unit_id);

CREATE TABLE learning_objective (
    id              UUID PRIMARY KEY,
    lesson_id       UUID          NOT NULL REFERENCES learning_lesson (id) ON DELETE CASCADE,
    statement       VARCHAR(300)  NOT NULL,
    display_order   INTEGER       NOT NULL,
    CONSTRAINT uq_learning_objective_order UNIQUE (lesson_id, display_order)
);

CREATE TABLE learning_section (
    id              UUID PRIMARY KEY,
    lesson_id       UUID          NOT NULL REFERENCES learning_lesson (id) ON DELETE CASCADE,
    section_type    VARCHAR(20)   NOT NULL,
    heading         VARCHAR(160)  NOT NULL,
    body            VARCHAR(1200) NOT NULL,
    example_kind    VARCHAR(20)   NOT NULL,
    display_order   INTEGER       NOT NULL,
    CONSTRAINT uq_learning_section_order UNIQUE (lesson_id, display_order),
    CONSTRAINT ck_learning_section_type CHECK (section_type IN (
        'INTRODUCTION', 'EXPLANATION', 'EXAMPLE', 'NOTE', 'WARNING', 'SUMMARY', 'REFERENCE', 'ACTIVITY'
    )),
    CONSTRAINT ck_learning_section_example CHECK (example_kind IN ('NONE', 'CONSTRUCTED', 'QUOTATION'))
);

CREATE TABLE learning_reference (
    id              UUID PRIMARY KEY,
    lesson_id       UUID          NOT NULL REFERENCES learning_lesson (id) ON DELETE CASCADE,
    target_kind     VARCHAR(40)   NOT NULL,
    target_slug     VARCHAR(180)  NOT NULL,
    note            VARCHAR(280),
    CONSTRAINT uq_learning_reference UNIQUE (lesson_id, target_kind, target_slug),
    CONSTRAINT ck_learning_reference_kind CHECK (target_kind IN (
        'DICTIONARY_ENTRY', 'GRAMMAR_RULE', 'GRAMMAR_CONCEPT', 'GRAMMAR_TOPIC',
        'SPELLING_RULE', 'RHETORIC_DEVICE', 'ARTICLE', 'MORPHOLOGY_TOOL'
    ))
);

CREATE TABLE learning_activity (
    id              UUID PRIMARY KEY,
    lesson_id       UUID          NOT NULL REFERENCES learning_lesson (id) ON DELETE CASCADE,
    activity_type   VARCHAR(32)   NOT NULL,
    title           VARCHAR(160)  NOT NULL,
    instructions    VARCHAR(800)  NOT NULL,
    display_order   INTEGER       NOT NULL,
    CONSTRAINT uq_learning_activity_order UNIQUE (lesson_id, display_order),
    CONSTRAINT ck_learning_activity_type CHECK (activity_type IN (
        'READ', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'MATCHING', 'CLASSIFICATION'
    ))
);

CREATE TABLE learning_quiz (
    id                  UUID PRIMARY KEY,
    lesson_id           UUID UNIQUE REFERENCES learning_lesson (id) ON DELETE CASCADE,
    unit_id             UUID UNIQUE REFERENCES learning_unit (id) ON DELETE CASCADE,
    title               VARCHAR(160) NOT NULL,
    passing_score       INTEGER      NOT NULL,
    max_attempts        INTEGER,
    content_version     INTEGER      NOT NULL,
    version             BIGINT       NOT NULL,
    CONSTRAINT ck_learning_quiz_parent CHECK (
        (lesson_id IS NOT NULL AND unit_id IS NULL) OR (lesson_id IS NULL AND unit_id IS NOT NULL)
    ),
    CONSTRAINT ck_learning_quiz_passing CHECK (passing_score BETWEEN 0 AND 100),
    CONSTRAINT ck_learning_quiz_attempts CHECK (max_attempts IS NULL OR max_attempts BETWEEN 1 AND 20)
);

CREATE TABLE learning_question (
    id                  UUID PRIMARY KEY,
    quiz_id             UUID          NOT NULL REFERENCES learning_quiz (id) ON DELETE CASCADE,
    prompt              VARCHAR(500)  NOT NULL,
    explanation         VARCHAR(800)  NOT NULL,
    question_type       VARCHAR(32)   NOT NULL,
    difficulty          VARCHAR(20)   NOT NULL,
    display_order       INTEGER       NOT NULL,
    knowledge_kind      VARCHAR(40),
    knowledge_slug      VARCHAR(180),
    CONSTRAINT uq_learning_question_order UNIQUE (quiz_id, display_order),
    CONSTRAINT ck_learning_question_type CHECK (question_type IN ('MULTIPLE_CHOICE', 'TRUE_FALSE', 'MULTIPLE_SELECT')),
    CONSTRAINT ck_learning_question_difficulty CHECK (difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE TABLE learning_question_option (
    id              UUID PRIMARY KEY,
    question_id     UUID          NOT NULL REFERENCES learning_question (id) ON DELETE CASCADE,
    label           VARCHAR(300)  NOT NULL,
    correct         BOOLEAN       NOT NULL,
    display_order   INTEGER       NOT NULL,
    CONSTRAINT uq_learning_option_order UNIQUE (question_id, display_order)
);

CREATE TABLE learning_attempt (
    id                  UUID PRIMARY KEY,
    token_hash          CHAR(64)     NOT NULL UNIQUE,
    quiz_id             UUID         NOT NULL REFERENCES learning_quiz (id),
    quiz_version        INTEGER      NOT NULL,
    snapshot            JSONB        NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    idempotency_key     VARCHAR(80),
    score               INTEGER,
    passed              BOOLEAN,
    result              JSONB,
    started_at          TIMESTAMPTZ  NOT NULL,
    expires_at          TIMESTAMPTZ  NOT NULL,
    submitted_at        TIMESTAMPTZ,
    version             BIGINT       NOT NULL,
    CONSTRAINT ck_learning_attempt_status CHECK (status IN ('OPEN', 'SUBMITTED'))
);

CREATE INDEX ix_learning_attempt_quiz ON learning_attempt (quiz_id, status);

CREATE TABLE learning_revision (
    id              UUID PRIMARY KEY,
    path_id         UUID         NOT NULL REFERENCES learning_path (id),
    revision        INTEGER      NOT NULL,
    status          VARCHAR(32)  NOT NULL,
    snapshot        JSONB        NOT NULL,
    created_by      UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_learning_revision_path ON learning_revision (path_id, revision);

INSERT INTO admin_permission (code, description) VALUES
    ('learning.path.view', 'View learning paths'),
    ('learning.path.manage', 'Create and edit learning paths'),
    ('learning.lesson.view', 'View lessons and previews'),
    ('learning.lesson.create', 'Create lessons'),
    ('learning.lesson.edit', 'Edit lessons'),
    ('learning.lesson.submit', 'Submit lessons for review'),
    ('learning.lesson.review', 'Review lessons'),
    ('learning.lesson.publish', 'Publish lessons'),
    ('learning.lesson.archive', 'Archive lessons'),
    ('learning.quiz.manage', 'Manage quizzes'),
    ('learning.question.manage', 'Manage quiz questions');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code FROM admin_permission WHERE code LIKE 'learning.%';

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000002', code FROM admin_permission WHERE code LIKE 'learning.%';

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000003', 'learning.path.view'),
    ('a0000000-0000-4000-8000-000000000003', 'learning.path.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'learning.lesson.view'),
    ('a0000000-0000-4000-8000-000000000003', 'learning.lesson.create'),
    ('a0000000-0000-4000-8000-000000000003', 'learning.lesson.edit'),
    ('a0000000-0000-4000-8000-000000000003', 'learning.lesson.submit'),
    ('a0000000-0000-4000-8000-000000000003', 'learning.quiz.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'learning.question.manage'),
    ('a0000000-0000-4000-8000-000000000004', 'learning.path.view'),
    ('a0000000-0000-4000-8000-000000000004', 'learning.lesson.view'),
    ('a0000000-0000-4000-8000-000000000004', 'learning.lesson.review'),
    ('a0000000-0000-4000-8000-000000000005', 'learning.path.view'),
    ('a0000000-0000-4000-8000-000000000005', 'learning.lesson.view'),
    ('a0000000-0000-4000-8000-000000000005', 'learning.lesson.publish'),
    ('a0000000-0000-4000-8000-000000000005', 'learning.lesson.archive'),
    ('a0000000-0000-4000-8000-000000000006', 'learning.path.view'),
    ('a0000000-0000-4000-8000-000000000006', 'learning.lesson.view');
