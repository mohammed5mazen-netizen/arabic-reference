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
    'QUESTION_CREATED',
    'REVIEW_ASSIGNED',
    'REVIEW_REASSIGNED',
    'EDITORIAL_COMMENT_ADDED',
    'EDITORIAL_COMMENT_RESOLVED',
    'QUALITY_SCAN_RUN',
    'SOURCE_UPDATED',
    'SOURCE_STATUS_CHANGED'
));

CREATE TABLE editorial_assignment (
    id               UUID         PRIMARY KEY,
    content_type     VARCHAR(40)  NOT NULL,
    content_id       UUID         NOT NULL,
    assignment_role  VARCHAR(20)  NOT NULL,
    assignee_id      UUID         NOT NULL,
    assigned_by      UUID         NOT NULL,
    assigned_at      TIMESTAMPTZ  NOT NULL,
    version          BIGINT       NOT NULL,
    CONSTRAINT uq_editorial_assignment UNIQUE (content_type, content_id, assignment_role),
    CONSTRAINT ck_editorial_assignment_role CHECK (assignment_role IN ('REVIEWER', 'PUBLISHER')),
    CONSTRAINT ck_editorial_assignment_version CHECK (version >= 0)
);

CREATE INDEX ix_editorial_assignment_assignee ON editorial_assignment (assignee_id, assignment_role);

CREATE TABLE editorial_comment (
    id            UUID         PRIMARY KEY,
    content_type  VARCHAR(40)  NOT NULL,
    content_id    UUID         NOT NULL,
    comment_type  VARCHAR(32)  NOT NULL,
    body          VARCHAR(1000) NOT NULL,
    status        VARCHAR(16)  NOT NULL,
    created_by    UUID         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    resolved_by   UUID,
    resolved_at   TIMESTAMPTZ,
    version       BIGINT       NOT NULL,
    CONSTRAINT ck_editorial_comment_type CHECK (comment_type IN (
        'GENERAL', 'CORRECTION', 'SOURCE_REQUIRED', 'RIGHTS_ISSUE', 'STRUCTURE', 'LANGUAGE', 'OTHER')),
    CONSTRAINT ck_editorial_comment_status CHECK (status IN ('OPEN', 'RESOLVED')),
    CONSTRAINT ck_editorial_comment_version CHECK (version >= 0)
);

CREATE INDEX ix_editorial_comment_target ON editorial_comment (content_type, content_id, created_at DESC);

CREATE TABLE quality_scan (
    id             UUID         PRIMARY KEY,
    scope          VARCHAR(16)  NOT NULL,
    content_type   VARCHAR(40),
    content_id     UUID,
    started_by     UUID         NOT NULL,
    started_at     TIMESTAMPTZ  NOT NULL,
    finished_at    TIMESTAMPTZ,
    record_count   INTEGER      NOT NULL,
    finding_count  INTEGER      NOT NULL,
    CONSTRAINT ck_quality_scan_scope CHECK (scope IN ('RECORD', 'TYPE', 'PUBLISHED')),
    CONSTRAINT ck_quality_scan_counts CHECK (record_count >= 0 AND finding_count >= 0)
);

CREATE TABLE quality_finding (
    id            UUID         PRIMARY KEY,
    scan_id       UUID         NOT NULL REFERENCES quality_scan (id),
    content_type  VARCHAR(40)  NOT NULL,
    content_id    UUID         NOT NULL,
    code          VARCHAR(64)  NOT NULL,
    severity      VARCHAR(16)  NOT NULL,
    message       VARCHAR(400) NOT NULL,
    field_name    VARCHAR(80),
    status        VARCHAR(16)  NOT NULL,
    detected_at   TIMESTAMPTZ  NOT NULL,
    stale_at      TIMESTAMPTZ,
    CONSTRAINT ck_quality_finding_severity CHECK (severity IN ('INFO', 'WARNING', 'BLOCKER')),
    CONSTRAINT ck_quality_finding_status CHECK (status IN ('OPEN', 'STALE'))
);

CREATE INDEX ix_quality_finding_open ON quality_finding (status, severity, content_type);
CREATE INDEX ix_quality_finding_target ON quality_finding (content_type, content_id, status);

CREATE VIEW editorial_record AS
SELECT 'DICTIONARY_ENTRY'::varchar(40) AS content_type, id, lemma_original AS title, slug, status,
       NULL::varchar(2000) AS summary, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL) AS has_snapshot
FROM lexical_entry
UNION ALL
SELECT 'MORPHOLOGY_ANALYSIS', a.id, e.lemma_original, e.slug, a.status, a.notes, a.created_by, a.updated_by, a.reviewed_by,
       a.created_at, a.updated_at, a.version, (a.published_snapshot IS NOT NULL)
FROM morphology_analysis a
JOIN lexical_entry e ON e.id = a.lexical_entry_id
UNION ALL
SELECT 'GRAMMAR_TOPIC', id, title_original, slug, status, summary, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM grammar_topic
UNION ALL
SELECT 'GRAMMAR_RULE', id, title_original, slug, status, summary, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM grammar_rule
UNION ALL
SELECT 'GRAMMAR_CONCEPT', id, term_original, slug, status, short_definition, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM grammar_concept
UNION ALL
SELECT 'SPELLING_TOPIC', id, title_original, slug, status, summary, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM spelling_topic
UNION ALL
SELECT 'SPELLING_RULE', id, title_original, slug, status, summary, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM spelling_rule
UNION ALL
SELECT 'RHETORIC_TOPIC', id, title_original, slug, status, summary, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM rhetoric_topic
UNION ALL
SELECT 'RHETORIC_DEVICE', id, name_original, slug, status, short_definition, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM rhetoric_device
UNION ALL
SELECT 'LITERARY_ERA', id, name_original, slug, status, summary, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM literary_era
UNION ALL
SELECT 'LITERARY_GENRE', id, name_original, slug, status, description, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM literary_genre
UNION ALL
SELECT 'LITERARY_SCHOOL', id, name_original, slug, status, description, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM literary_school
UNION ALL
SELECT 'LITERARY_FIGURE', id, canonical_name, slug, status, biography_summary, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM literary_figure
UNION ALL
SELECT 'LITERARY_WORK', id, title_original, slug, status, description, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM literary_work
UNION ALL
SELECT 'ARTICLE', id, title_original, slug, status, excerpt, created_by, updated_by, reviewed_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM article
UNION ALL
SELECT 'LEARNING_PATH', id, title, slug, status, summary, created_by, updated_by, verified_by, created_at, updated_at, version,
       (published_snapshot IS NOT NULL)
FROM learning_path;

ALTER TABLE reference_source ADD COLUMN identity_key VARCHAR(700);
UPDATE reference_source
SET identity_key = lower(btrim(title)) || '|' || lower(btrim(coalesce(author, ''))) || '|' || lower(btrim(coalesce(edition, '')));
ALTER TABLE reference_source ALTER COLUMN identity_key SET NOT NULL;
CREATE INDEX ix_reference_source_identity ON reference_source (identity_key);

INSERT INTO admin_permission (code, description) VALUES
    ('editorial.dashboard.view', 'View the editorial operations dashboard'),
    ('editorial.queue.view', 'View the unified editorial queue'),
    ('editorial.review.assign', 'Assign a reviewer or publisher'),
    ('editorial.comment.create', 'Add an internal editorial comment'),
    ('editorial.comment.resolve', 'Resolve an internal editorial comment'),
    ('editorial.quality.view', 'View quality findings'),
    ('editorial.quality.run', 'Run a bounded quality scan'),
    ('editorial.diff.view', 'View an editorial diff'),
    ('source.usage.view', 'View where a source is cited');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code
FROM admin_permission
WHERE code IN (
    'editorial.dashboard.view',
    'editorial.queue.view',
    'editorial.review.assign',
    'editorial.comment.create',
    'editorial.comment.resolve',
    'editorial.quality.view',
    'editorial.quality.run',
    'editorial.diff.view',
    'source.usage.view'
);

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'editorial.dashboard.view'),
    ('a0000000-0000-4000-8000-000000000002', 'editorial.queue.view'),
    ('a0000000-0000-4000-8000-000000000002', 'editorial.review.assign'),
    ('a0000000-0000-4000-8000-000000000002', 'editorial.comment.create'),
    ('a0000000-0000-4000-8000-000000000002', 'editorial.comment.resolve'),
    ('a0000000-0000-4000-8000-000000000002', 'editorial.quality.view'),
    ('a0000000-0000-4000-8000-000000000002', 'editorial.quality.run'),
    ('a0000000-0000-4000-8000-000000000002', 'editorial.diff.view'),
    ('a0000000-0000-4000-8000-000000000002', 'source.usage.view'),
    ('a0000000-0000-4000-8000-000000000003', 'editorial.dashboard.view'),
    ('a0000000-0000-4000-8000-000000000003', 'editorial.queue.view'),
    ('a0000000-0000-4000-8000-000000000003', 'editorial.comment.create'),
    ('a0000000-0000-4000-8000-000000000003', 'editorial.comment.resolve'),
    ('a0000000-0000-4000-8000-000000000003', 'source.usage.view'),
    ('a0000000-0000-4000-8000-000000000004', 'editorial.dashboard.view'),
    ('a0000000-0000-4000-8000-000000000004', 'editorial.queue.view'),
    ('a0000000-0000-4000-8000-000000000004', 'editorial.review.assign'),
    ('a0000000-0000-4000-8000-000000000004', 'editorial.comment.create'),
    ('a0000000-0000-4000-8000-000000000004', 'editorial.comment.resolve'),
    ('a0000000-0000-4000-8000-000000000004', 'editorial.quality.view'),
    ('a0000000-0000-4000-8000-000000000004', 'editorial.diff.view'),
    ('a0000000-0000-4000-8000-000000000004', 'source.usage.view'),
    ('a0000000-0000-4000-8000-000000000005', 'editorial.dashboard.view'),
    ('a0000000-0000-4000-8000-000000000005', 'editorial.queue.view'),
    ('a0000000-0000-4000-8000-000000000005', 'editorial.quality.view'),
    ('a0000000-0000-4000-8000-000000000005', 'editorial.quality.run'),
    ('a0000000-0000-4000-8000-000000000005', 'editorial.diff.view'),
    ('a0000000-0000-4000-8000-000000000005', 'source.usage.view'),
    ('a0000000-0000-4000-8000-000000000006', 'editorial.dashboard.view'),
    ('a0000000-0000-4000-8000-000000000006', 'editorial.queue.view'),
    ('a0000000-0000-4000-8000-000000000006', 'editorial.quality.view'),
    ('a0000000-0000-4000-8000-000000000006', 'editorial.diff.view'),
    ('a0000000-0000-4000-8000-000000000006', 'source.usage.view');
