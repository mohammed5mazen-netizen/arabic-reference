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
    'GRAMMAR_CONTENT_ARCHIVED'
));

CREATE TABLE grammar_topic (
    id                      UUID         PRIMARY KEY,
    parent_id               UUID         NULL REFERENCES grammar_topic (id),
    title_original          VARCHAR(160) NOT NULL,
    title_normalized        VARCHAR(160) NOT NULL,
    published_title         VARCHAR(160) NULL,
    published_normalized    VARCHAR(160) NULL,
    summary                 VARCHAR(1000) NULL,
    published_summary       VARCHAR(1000) NULL,
    slug                    VARCHAR(180) NOT NULL,
    display_order           INTEGER      NOT NULL,
    published_display_order INTEGER      NULL,
    published_parent_id     UUID         NULL,
    category                VARCHAR(40)  NOT NULL,
    published_category      VARCHAR(40)  NULL,
    difficulty              VARCHAR(20)  NULL,
    status                  VARCHAR(32)  NOT NULL,
    reviewed_by             UUID         NULL,
    change_reason           VARCHAR(500) NULL,
    published_snapshot      JSONB        NULL,
    created_at              TIMESTAMPTZ  NOT NULL,
    updated_at              TIMESTAMPTZ  NOT NULL,
    created_by              UUID         NOT NULL,
    updated_by              UUID         NOT NULL,
    version                 BIGINT       NOT NULL,
    CONSTRAINT uq_grammar_topic_slug UNIQUE (slug),
    CONSTRAINT ck_grammar_topic_parent CHECK (parent_id IS DISTINCT FROM id),
    CONSTRAINT ck_grammar_topic_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT ck_grammar_topic_category CHECK (category IN ('FOUNDATIONS', 'NOMINAL_SENTENCE', 'VERBAL_SENTENCE', 'MARFUAT', 'MANSUBAT', 'MAJRURAT', 'TAWABI', 'NAWASIKH', 'ASALIB', 'NUMERALS', 'OTHER')),
    CONSTRAINT ck_grammar_topic_difficulty CHECK (difficulty IS NULL OR difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE INDEX ix_grammar_topic_parent ON grammar_topic (parent_id, display_order);
CREATE INDEX ix_grammar_topic_status ON grammar_topic (status, updated_at DESC);
CREATE INDEX ix_grammar_topic_published_parent ON grammar_topic (published_parent_id, published_display_order);
CREATE INDEX ix_grammar_topic_published_norm ON grammar_topic (published_normalized);

CREATE TABLE grammar_topic_prerequisite (
    topic_id          UUID NOT NULL REFERENCES grammar_topic (id),
    required_topic_id UUID NOT NULL REFERENCES grammar_topic (id),
    PRIMARY KEY (topic_id, required_topic_id),
    CONSTRAINT ck_grammar_topic_prerequisite_self CHECK (topic_id IS DISTINCT FROM required_topic_id)
);

CREATE TABLE grammar_topic_citation (
    topic_id    UUID NOT NULL REFERENCES grammar_topic (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (topic_id, citation_id)
);

CREATE TABLE grammar_rule (
    id                      UUID         PRIMARY KEY,
    topic_id                UUID         NOT NULL REFERENCES grammar_topic (id),
    title_original          VARCHAR(160) NOT NULL,
    title_normalized        VARCHAR(160) NOT NULL,
    published_title         VARCHAR(160) NULL,
    published_normalized    VARCHAR(160) NULL,
    summary                 VARCHAR(1000) NULL,
    published_summary       VARCHAR(1000) NULL,
    slug                    VARCHAR(180) NOT NULL,
    rule_text               VARCHAR(4000) NULL,
    display_order           INTEGER      NOT NULL,
    published_display_order INTEGER      NULL,
    published_topic_id      UUID         NULL,
    difficulty              VARCHAR(20)  NULL,
    status                  VARCHAR(32)  NOT NULL,
    reviewed_by             UUID         NULL,
    change_reason           VARCHAR(500) NULL,
    published_snapshot      JSONB        NULL,
    created_at              TIMESTAMPTZ  NOT NULL,
    updated_at              TIMESTAMPTZ  NOT NULL,
    created_by              UUID         NOT NULL,
    updated_by              UUID         NOT NULL,
    version                 BIGINT       NOT NULL,
    CONSTRAINT uq_grammar_rule_slug UNIQUE (slug),
    CONSTRAINT ck_grammar_rule_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED')),
    CONSTRAINT ck_grammar_rule_difficulty CHECK (difficulty IS NULL OR difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED'))
);

CREATE INDEX ix_grammar_rule_topic ON grammar_rule (topic_id, display_order);
CREATE INDEX ix_grammar_rule_published_topic ON grammar_rule (published_topic_id, published_display_order);
CREATE INDEX ix_grammar_rule_status ON grammar_rule (status, updated_at DESC);
CREATE INDEX ix_grammar_rule_published_norm ON grammar_rule (published_normalized);

CREATE TABLE grammar_rule_component (
    id            UUID         PRIMARY KEY,
    rule_id       UUID         NOT NULL REFERENCES grammar_rule (id),
    component_type VARCHAR(32) NOT NULL,
    heading       VARCHAR(160) NULL,
    body          VARCHAR(4000) NOT NULL,
    display_order INTEGER      NOT NULL,
    CONSTRAINT ck_grammar_component_type CHECK (component_type IN (
        'DEFINITION', 'CORE_RULE', 'CONDITION', 'EXCEPTION', 'NOTE', 'WARNING', 'TERMINOLOGY', 'DIFFERENCE', 'SCHOLARLY_NOTE'
    ))
);

CREATE INDEX ix_grammar_component_rule ON grammar_rule_component (rule_id, display_order);

CREATE TABLE grammar_rule_citation (
    rule_id     UUID NOT NULL REFERENCES grammar_rule (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (rule_id, citation_id)
);

CREATE TABLE grammar_component_citation (
    component_id UUID NOT NULL REFERENCES grammar_rule_component (id),
    citation_id  UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (component_id, citation_id)
);

CREATE TABLE grammar_concept (
    id                   UUID         PRIMARY KEY,
    term_original        VARCHAR(160) NOT NULL,
    term_normalized      VARCHAR(160) NOT NULL,
    published_title      VARCHAR(160) NULL,
    published_normalized VARCHAR(160) NULL,
    slug                 VARCHAR(180) NOT NULL,
    short_definition     VARCHAR(500) NULL,
    published_summary    VARCHAR(500) NULL,
    detailed_definition  VARCHAR(4000) NULL,
    status               VARCHAR(32)  NOT NULL,
    reviewed_by          UUID         NULL,
    change_reason        VARCHAR(500) NULL,
    published_snapshot   JSONB        NULL,
    created_at           TIMESTAMPTZ  NOT NULL,
    updated_at           TIMESTAMPTZ  NOT NULL,
    created_by           UUID         NOT NULL,
    updated_by           UUID         NOT NULL,
    version              BIGINT       NOT NULL,
    CONSTRAINT uq_grammar_concept_slug UNIQUE (slug),
    CONSTRAINT ck_grammar_concept_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_grammar_concept_status ON grammar_concept (status, updated_at DESC);
CREATE INDEX ix_grammar_concept_published_norm ON grammar_concept (published_normalized);

CREATE TABLE grammar_concept_alias (
    id                   UUID         PRIMARY KEY,
    concept_id           UUID         NOT NULL REFERENCES grammar_concept (id),
    alias_original       VARCHAR(160) NOT NULL,
    alias_normalized     VARCHAR(160) NOT NULL,
    published_normalized VARCHAR(160) NULL,
    CONSTRAINT uq_grammar_concept_alias UNIQUE (concept_id, alias_normalized)
);

CREATE INDEX ix_grammar_alias_published_norm ON grammar_concept_alias (published_normalized);

CREATE TABLE grammar_concept_citation (
    concept_id  UUID NOT NULL REFERENCES grammar_concept (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (concept_id, citation_id)
);

CREATE TABLE grammar_concept_rule (
    concept_id UUID NOT NULL REFERENCES grammar_concept (id),
    rule_id    UUID NOT NULL REFERENCES grammar_rule (id),
    PRIMARY KEY (concept_id, rule_id)
);

CREATE TABLE grammar_rule_relation (
    id             UUID        PRIMARY KEY,
    source_rule_id UUID        NOT NULL REFERENCES grammar_rule (id),
    target_rule_id UUID        NOT NULL REFERENCES grammar_rule (id),
    relation_type  VARCHAR(32) NOT NULL,
    CONSTRAINT uq_grammar_rule_relation UNIQUE (source_rule_id, target_rule_id, relation_type),
    CONSTRAINT ck_grammar_rule_relation_self CHECK (source_rule_id IS DISTINCT FROM target_rule_id),
    CONSTRAINT ck_grammar_rule_relation_type CHECK (relation_type IN (
        'RELATED_TO', 'PREREQUISITE_OF', 'EXCEPTION_TO', 'SPECIAL_CASE_OF', 'CONTRASTS_WITH', 'SEE_ALSO'
    ))
);

CREATE INDEX ix_grammar_rule_relation_target ON grammar_rule_relation (target_rule_id);

CREATE TABLE grammar_role (
    id         UUID         PRIMARY KEY,
    code       VARCHAR(40)  NOT NULL,
    label_ar   VARCHAR(80)  NOT NULL,
    state_kind VARCHAR(20)  NOT NULL,
    active     BOOLEAN      NOT NULL,
    CONSTRAINT uq_grammar_role_code UNIQUE (code),
    CONSTRAINT ck_grammar_role_state CHECK (state_kind IN ('NOMINAL_CASE', 'VERBAL_MOOD', 'UNSPECIFIED'))
);

INSERT INTO grammar_role (id, code, label_ar, state_kind, active) VALUES
    ('c0000000-0000-4000-8000-000000000001', 'MUBTADA', 'مبتدأ', 'NOMINAL_CASE', true),
    ('c0000000-0000-4000-8000-000000000002', 'KHABAR', 'خبر', 'NOMINAL_CASE', true),
    ('c0000000-0000-4000-8000-000000000003', 'FAIL', 'فاعل', 'NOMINAL_CASE', true),
    ('c0000000-0000-4000-8000-000000000004', 'NAIB_FAIL', 'نائب فاعل', 'NOMINAL_CASE', true),
    ('c0000000-0000-4000-8000-000000000005', 'MAFUL_BIHI', 'مفعول به', 'NOMINAL_CASE', true),
    ('c0000000-0000-4000-8000-000000000006', 'HAL', 'حال', 'NOMINAL_CASE', true),
    ('c0000000-0000-4000-8000-000000000007', 'TAMYIZ', 'تمييز', 'NOMINAL_CASE', true),
    ('c0000000-0000-4000-8000-000000000008', 'NAAT', 'نعت', 'NOMINAL_CASE', true),
    ('c0000000-0000-4000-8000-000000000009', 'MUDAF_ILAYH', 'مضاف إليه', 'NOMINAL_CASE', true),
    ('c0000000-0000-4000-8000-00000000000a', 'FI3L', 'فعل', 'VERBAL_MOOD', true);

CREATE TABLE grammar_annotation (
    id                 UUID         PRIMARY KEY,
    sentence_original  VARCHAR(500) NOT NULL,
    sentence_normalized VARCHAR(500) NOT NULL,
    citation_id        UUID         NULL REFERENCES source_citation (id),
    status             VARCHAR(32)  NOT NULL,
    reviewed_by        UUID         NULL,
    change_reason      VARCHAR(500) NULL,
    published_snapshot JSONB        NULL,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,
    created_by         UUID         NOT NULL,
    updated_by         UUID         NOT NULL,
    version            BIGINT       NOT NULL,
    CONSTRAINT ck_grammar_annotation_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_grammar_annotation_status ON grammar_annotation (status, updated_at DESC);

CREATE TABLE grammar_example (
    id              UUID         PRIMARY KEY,
    rule_id         UUID         NOT NULL REFERENCES grammar_rule (id),
    component_id    UUID         NULL REFERENCES grammar_rule_component (id),
    text_original   VARCHAR(1000) NOT NULL,
    text_normalized VARCHAR(1000) NOT NULL,
    explanation     VARCHAR(1000) NULL,
    example_type    VARCHAR(32)  NOT NULL,
    citation_id     UUID         NULL REFERENCES source_citation (id),
    surah           INTEGER      NULL,
    ayah            INTEGER      NULL,
    poet            VARCHAR(160) NULL,
    work_title      VARCHAR(160) NULL,
    verse_locator   VARCHAR(80)  NULL,
    annotation_id   UUID         NULL REFERENCES grammar_annotation (id),
    display_order   INTEGER      NOT NULL,
    CONSTRAINT ck_grammar_example_type CHECK (example_type IN (
        'CONSTRUCTED', 'QUOTED', 'QURANIC', 'POETRY', 'PROSE', 'COUNTEREXAMPLE', 'OTHER'
    )),
    CONSTRAINT ck_grammar_example_surah CHECK (surah IS NULL OR surah BETWEEN 1 AND 114),
    CONSTRAINT ck_grammar_example_ayah CHECK (ayah IS NULL OR ayah >= 1)
);

CREATE INDEX ix_grammar_example_rule ON grammar_example (rule_id, display_order);

CREATE TABLE grammar_token (
    id                      UUID         PRIMARY KEY,
    annotation_id           UUID         NOT NULL REFERENCES grammar_annotation (id),
    surface                 VARCHAR(80)  NOT NULL,
    normalized              VARCHAR(80)  NOT NULL,
    position                INTEGER      NOT NULL,
    lexical_entry_id        UUID         NULL REFERENCES lexical_entry (id),
    morphology_analysis_id  UUID         NULL REFERENCES morphology_analysis (id),
    role_code               VARCHAR(40)  NULL REFERENCES grammar_role (code),
    grammatical_state       VARCHAR(16)  NULL,
    explanation             VARCHAR(500) NULL,
    CONSTRAINT uq_grammar_token_position UNIQUE (annotation_id, position),
    CONSTRAINT ck_grammar_token_position CHECK (position >= 0),
    CONSTRAINT ck_grammar_token_state CHECK (grammatical_state IS NULL OR grammatical_state IN ('RAFA', 'NASB', 'JARR', 'JAZM'))
);

CREATE INDEX ix_grammar_token_annotation ON grammar_token (annotation_id, position);

CREATE TABLE grammar_dependency (
    id                 UUID        PRIMARY KEY,
    annotation_id      UUID        NOT NULL REFERENCES grammar_annotation (id),
    governor_position  INTEGER     NOT NULL,
    dependent_position INTEGER     NOT NULL,
    relation_label     VARCHAR(80) NOT NULL,
    CONSTRAINT uq_grammar_dependency UNIQUE (annotation_id, governor_position, dependent_position, relation_label),
    CONSTRAINT ck_grammar_dependency_positions CHECK (
        governor_position >= 0 AND dependent_position >= 0 AND governor_position <> dependent_position
    )
);

CREATE INDEX ix_grammar_dependency_annotation ON grammar_dependency (annotation_id);

INSERT INTO admin_permission (code, description) VALUES
    ('grammar.topic.view', 'View grammar topics'),
    ('grammar.topic.manage', 'Create and edit grammar topics'),
    ('grammar.rule.view', 'View grammar rules'),
    ('grammar.rule.create', 'Create a grammar rule'),
    ('grammar.rule.edit', 'Edit a grammar rule and its components'),
    ('grammar.rule.submit', 'Submit grammar content for review'),
    ('grammar.rule.review', 'Review grammar content'),
    ('grammar.rule.publish', 'Publish verified grammar content'),
    ('grammar.rule.archive', 'Archive published grammar content'),
    ('grammar.concept.view', 'View grammar concepts'),
    ('grammar.concept.manage', 'Create and edit grammar concepts'),
    ('grammar.example.manage', 'Add grammar examples'),
    ('grammar.annotation.manage', 'Manage manual syntax annotations and grammatical roles');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code
FROM admin_permission
WHERE code LIKE 'grammar.%';

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'grammar.topic.view'),
    ('a0000000-0000-4000-8000-000000000002', 'grammar.rule.view'),
    ('a0000000-0000-4000-8000-000000000002', 'grammar.concept.view'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.topic.view'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.topic.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.rule.view'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.rule.create'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.rule.edit'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.rule.submit'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.concept.view'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.concept.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.example.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'grammar.annotation.manage'),
    ('a0000000-0000-4000-8000-000000000004', 'grammar.topic.view'),
    ('a0000000-0000-4000-8000-000000000004', 'grammar.rule.view'),
    ('a0000000-0000-4000-8000-000000000004', 'grammar.concept.view'),
    ('a0000000-0000-4000-8000-000000000004', 'grammar.rule.review'),
    ('a0000000-0000-4000-8000-000000000005', 'grammar.topic.view'),
    ('a0000000-0000-4000-8000-000000000005', 'grammar.rule.view'),
    ('a0000000-0000-4000-8000-000000000005', 'grammar.concept.view'),
    ('a0000000-0000-4000-8000-000000000005', 'grammar.rule.publish'),
    ('a0000000-0000-4000-8000-000000000005', 'grammar.rule.archive');
