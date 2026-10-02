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
    'MORPHOLOGY_RULE_CHANGED'
));

CREATE TABLE morphological_pattern (
    id                  UUID         PRIMARY KEY,
    code                VARCHAR(40)  NOT NULL,
    pattern_original    VARCHAR(40)  NOT NULL,
    pattern_normalized  VARCHAR(40)  NOT NULL,
    category            VARCHAR(40)  NOT NULL,
    radical_count       SMALLINT     NOT NULL,
    description         VARCHAR(300) NULL,
    status              VARCHAR(16)  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    version             BIGINT       NOT NULL,
    CONSTRAINT uq_morphological_pattern_code UNIQUE (code),
    CONSTRAINT ck_morphological_pattern_status CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT ck_morphological_pattern_radicals CHECK (radical_count BETWEEN 2 AND 5)
);

CREATE INDEX ix_morphological_pattern_status ON morphological_pattern (status, code);

CREATE TABLE morphology_analysis (
    id                 UUID         PRIMARY KEY,
    lexical_entry_id   UUID         NOT NULL REFERENCES lexical_entry (id),
    pattern_id         UUID         NULL REFERENCES morphological_pattern (id),
    derivation         VARCHAR(40)  NULL,
    verb_class         VARCHAR(32)  NULL,
    imperfect_vowel    VARCHAR(16)  NULL,
    notes              VARCHAR(500) NULL,
    features           JSONB        NOT NULL,
    segmentation       JSONB        NOT NULL,
    status             VARCHAR(32)  NOT NULL,
    reviewed_by        UUID         NULL,
    change_reason      VARCHAR(500) NULL,
    published_snapshot JSONB        NULL,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    created_by         UUID         NOT NULL,
    updated_by         UUID         NOT NULL,
    version            BIGINT       NOT NULL
);

CREATE INDEX ix_morphology_analysis_entry ON morphology_analysis (lexical_entry_id);
CREATE INDEX ix_morphology_analysis_status ON morphology_analysis (status, updated_at DESC);
CREATE INDEX ix_morphology_analysis_published ON morphology_analysis (lexical_entry_id) WHERE published_snapshot IS NOT NULL;

CREATE TABLE morphology_analysis_citation (
    analysis_id UUID NOT NULL REFERENCES morphology_analysis (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (analysis_id, citation_id)
);

CREATE TABLE morphology_rule (
    code              VARCHAR(40)  PRIMARY KEY,
    explanation_code  VARCHAR(80)  NOT NULL,
    description       VARCHAR(300) NOT NULL,
    enabled           BOOLEAN      NOT NULL,
    rule_set_version  VARCHAR(40)  NOT NULL,
    version           BIGINT       NOT NULL
);

CREATE TABLE morphology_state (
    id         SMALLINT PRIMARY KEY,
    generation BIGINT   NOT NULL,
    CONSTRAINT ck_morphology_state_singleton CHECK (id = 1)
);

INSERT INTO morphology_state (id, generation) VALUES (1, 1);

INSERT INTO morphological_pattern (
    id, code, pattern_original, pattern_normalized, category, radical_count, description, status, created_at, updated_at, version
) VALUES
    ('b0000000-0000-4000-8000-000000000001', 'FA3ALA', 'فَعَلَ', 'فعل', 'VERB', 3, 'الثلاثي المجرد على فَعَلَ', 'ACTIVE', now(), now(), 0),
    ('b0000000-0000-4000-8000-000000000002', 'FA33ALA', 'فَعَّلَ', 'فعل', 'VERB', 3, 'المزيد بالتضعيف', 'ACTIVE', now(), now(), 0),
    ('b0000000-0000-4000-8000-000000000003', 'AF3ALA', 'أَفْعَلَ', 'افعل', 'VERB', 3, 'المزيد بهمزة القطع', 'ACTIVE', now(), now(), 0),
    ('b0000000-0000-4000-8000-000000000004', 'ISTAF3ALA', 'اِسْتَفْعَلَ', 'استفعل', 'VERB', 3, 'المزيد بالسين والتاء', 'ACTIVE', now(), now(), 0),
    ('b0000000-0000-4000-8000-000000000005', 'FA3IL', 'فَاعِل', 'فاعل', 'ACTIVE_PARTICIPLE', 3, 'اسم الفاعل من الثلاثي', 'ACTIVE', now(), now(), 0),
    ('b0000000-0000-4000-8000-000000000006', 'MAF3UL', 'مَفْعُول', 'مفعول', 'PASSIVE_PARTICIPLE', 3, 'اسم المفعول من الثلاثي', 'ACTIVE', now(), now(), 0),
    ('b0000000-0000-4000-8000-000000000007', 'FA3L', 'فَعْل', 'فعل', 'VERBAL_NOUN', 3, 'مصدر على فَعْل، يُربط يدويًا ولا يُولَّد', 'ACTIVE', now(), now(), 0),
    ('b0000000-0000-4000-8000-000000000008', 'FA3LALA', 'فَعْلَلَ', 'فعلل', 'VERB', 4, 'الرباعي المجرد', 'ACTIVE', now(), now(), 0);

INSERT INTO morphology_rule (code, explanation_code, description, enabled, rule_set_version, version) VALUES
    ('R-DICT', 'DICTIONARY_LEMMA', 'مطابقة الصيغة المعجمية المنشورة', true, 's3-sound-2026-10-03', 0),
    ('R-AL', 'DEFINITE_ARTICLE', 'فصل أداة التعريف ال', true, 's3-sound-2026-10-03', 0),
    ('R-CONJ', 'CONJUNCTION_CLITIC', 'فصل واو أو فاء العطف', true, 's3-sound-2026-10-03', 0),
    ('R-PREP', 'PROCLITIC', 'فصل باء أو لام أو كاف', true, 's3-sound-2026-10-03', 0),
    ('R-SUFFIX', 'ATTACHED_PRONOUN', 'فصل ضمير متصل من قائمة محدودة', true, 's3-sound-2026-10-03', 0),
    ('R-FA3IL', 'PUBLISHED_ROOT_ACTIVE_PARTICIPLE', 'اسم فاعل إذا وُجد فعل منشور على الجذر', true, 's3-sound-2026-10-03', 0),
    ('R-MAF3UL', 'PUBLISHED_ROOT_PASSIVE_PARTICIPLE', 'اسم مفعول إذا وُجد فعل منشور على الجذر', true, 's3-sound-2026-10-03', 0),
    ('R-MANUAL', 'MANUAL_RECORD', 'تحليل موثّق منشور', true, 's3-sound-2026-10-03', 0);

INSERT INTO admin_permission (code, description) VALUES
    ('morphology.view', 'View morphology records'),
    ('morphology.pattern.manage', 'Create and edit morphological patterns'),
    ('morphology.analysis.create', 'Create a manual morphological analysis'),
    ('morphology.analysis.edit', 'Edit or submit a manual morphological analysis'),
    ('morphology.analysis.review', 'Review a manual morphological analysis'),
    ('morphology.analysis.publish', 'Publish or archive a verified morphological analysis'),
    ('morphology.rule.view', 'View morphology rules'),
    ('morphology.rule.manage', 'Enable or disable morphology rules');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code
FROM admin_permission
WHERE code LIKE 'morphology.%';

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'morphology.view'),
    ('a0000000-0000-4000-8000-000000000003', 'morphology.view'),
    ('a0000000-0000-4000-8000-000000000003', 'morphology.analysis.create'),
    ('a0000000-0000-4000-8000-000000000003', 'morphology.analysis.edit'),
    ('a0000000-0000-4000-8000-000000000003', 'morphology.rule.view'),
    ('a0000000-0000-4000-8000-000000000004', 'morphology.view'),
    ('a0000000-0000-4000-8000-000000000004', 'morphology.analysis.review'),
    ('a0000000-0000-4000-8000-000000000004', 'morphology.rule.view'),
    ('a0000000-0000-4000-8000-000000000005', 'morphology.view'),
    ('a0000000-0000-4000-8000-000000000005', 'morphology.analysis.publish');
