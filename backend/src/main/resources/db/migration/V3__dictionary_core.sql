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
    'CONTENT_ARCHIVED'
));

CREATE TABLE linguistic_root (
    id                     UUID         PRIMARY KEY,
    root_original          VARCHAR(32)  NOT NULL,
    root_normalized        VARCHAR(32)  NOT NULL,
    radical_count          SMALLINT     NOT NULL,
    notes                  VARCHAR(500) NULL,
    status                 VARCHAR(32)  NOT NULL,
    slug                   VARCHAR(80)  NOT NULL,
    published_normalized   VARCHAR(32)  NULL,
    published_snapshot     JSONB        NULL,
    reviewed_by            UUID         NULL,
    change_reason          VARCHAR(500) NULL,
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    created_by             UUID         NOT NULL,
    updated_by             UUID         NOT NULL,
    version                BIGINT       NOT NULL,
    CONSTRAINT uq_linguistic_root_normalized UNIQUE (root_normalized),
    CONSTRAINT uq_linguistic_root_slug UNIQUE (slug),
    CONSTRAINT ck_linguistic_root_radicals CHECK (radical_count BETWEEN 2 AND 6),
    CONSTRAINT ck_linguistic_root_status CHECK (status IN (
        'DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_linguistic_root_status ON linguistic_root (status);
CREATE INDEX ix_linguistic_root_published ON linguistic_root (published_normalized);

CREATE TABLE reference_source (
    id                  UUID         PRIMARY KEY,
    source_type         VARCHAR(32)  NOT NULL,
    title               VARCHAR(300) NOT NULL,
    author              VARCHAR(200) NULL,
    publisher           VARCHAR(200) NULL,
    edition             VARCHAR(80)  NULL,
    publication_year    INTEGER      NULL,
    isbn                VARCHAR(32)  NULL,
    url                 VARCHAR(500) NULL,
    license_type        VARCHAR(32)  NOT NULL,
    public_domain       BOOLEAN      NOT NULL,
    attribution_text    VARCHAR(500) NOT NULL,
    notes               VARCHAR(1000) NULL,
    status              VARCHAR(32)  NOT NULL,
    slug                VARCHAR(160) NOT NULL,
    published_snapshot  JSONB        NULL,
    reviewed_by         UUID         NULL,
    change_reason       VARCHAR(500) NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    created_by          UUID         NOT NULL,
    updated_by          UUID         NOT NULL,
    version             BIGINT       NOT NULL,
    CONSTRAINT uq_reference_source_slug UNIQUE (slug),
    CONSTRAINT ck_reference_source_year CHECK (publication_year IS NULL OR publication_year BETWEEN 1 AND 2100),
    CONSTRAINT ck_reference_source_type CHECK (source_type IN (
        'DICTIONARY', 'BOOK', 'JOURNAL', 'WEBSITE', 'CORPUS', 'MANUSCRIPT', 'OTHER')),
    CONSTRAINT ck_reference_source_license CHECK (license_type IN (
        'PUBLIC_DOMAIN', 'CC0', 'CC_BY', 'CC_BY_SA', 'PERMISSION_GRANTED', 'RESTRICTED', 'UNKNOWN')),
    CONSTRAINT ck_reference_source_status CHECK (status IN (
        'DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_reference_source_status ON reference_source (status);
CREATE INDEX ix_reference_source_title ON reference_source (title);

CREATE TABLE lexical_entry (
    id                          UUID         PRIMARY KEY,
    lemma_original              VARCHAR(80)  NOT NULL,
    lemma_normalized            VARCHAR(80)  NOT NULL,
    vocalized_form              VARCHAR(80)  NULL,
    root_id                     UUID         NULL REFERENCES linguistic_root (id),
    part_of_speech              VARCHAR(32)  NOT NULL,
    gender                      VARCHAR(32)  NULL,
    status                      VARCHAR(32)  NOT NULL,
    slug                        VARCHAR(120) NOT NULL,
    published_lemma_normalized  VARCHAR(80)  NULL,
    published_snapshot          JSONB        NULL,
    reviewed_by                 UUID         NULL,
    change_reason               VARCHAR(500) NULL,
    created_at                  TIMESTAMPTZ  NOT NULL,
    updated_at                  TIMESTAMPTZ  NOT NULL,
    created_by                  UUID         NOT NULL,
    updated_by                  UUID         NOT NULL,
    version                     BIGINT       NOT NULL,
    CONSTRAINT uq_lexical_entry_slug UNIQUE (slug),
    CONSTRAINT ck_lexical_entry_pos CHECK (part_of_speech IN (
        'NOUN', 'VERB', 'ADJECTIVE', 'ADVERB', 'PRONOUN', 'PREPOSITION', 'CONJUNCTION',
        'PARTICLE', 'INTERJECTION', 'PROPER_NOUN', 'OTHER')),
    CONSTRAINT ck_lexical_entry_gender CHECK (gender IS NULL OR gender IN (
        'MASCULINE', 'FEMININE', 'COMMON', 'NOT_APPLICABLE')),
    CONSTRAINT ck_lexical_entry_status CHECK (status IN (
        'DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_lexical_entry_lemma ON lexical_entry (lemma_normalized);
CREATE INDEX ix_lexical_entry_published_lemma ON lexical_entry (published_lemma_normalized);
CREATE INDEX ix_lexical_entry_status ON lexical_entry (status);
CREATE INDEX ix_lexical_entry_root ON lexical_entry (root_id);

CREATE TABLE lexical_sense (
    id                UUID          PRIMARY KEY,
    lexical_entry_id  UUID          NOT NULL REFERENCES lexical_entry (id),
    definition        VARCHAR(4000) NOT NULL,
    short_definition  VARCHAR(280)  NULL,
    usage_label       VARCHAR(32)   NULL,
    domain_label      VARCHAR(32)   NULL,
    display_order     INTEGER       NOT NULL,
    status            VARCHAR(32)   NOT NULL,
    created_at        TIMESTAMPTZ   NOT NULL,
    updated_at        TIMESTAMPTZ   NOT NULL,
    created_by        UUID          NOT NULL,
    updated_by        UUID          NOT NULL,
    version           BIGINT        NOT NULL,
    CONSTRAINT ck_lexical_sense_order CHECK (display_order >= 1),
    CONSTRAINT ck_lexical_sense_usage CHECK (usage_label IS NULL OR usage_label IN (
        'CLASSICAL', 'ARCHAIC', 'MODERN', 'COLLOQUIAL', 'CONVENTIONAL', 'FIGURATIVE', 'RARE', 'TECHNICAL')),
    CONSTRAINT ck_lexical_sense_domain CHECK (domain_label IS NULL OR domain_label IN (
        'GENERAL', 'LANGUAGE', 'MEDICINE', 'TECHNOLOGY', 'ECONOMICS', 'LAW', 'RELIGION', 'LITERATURE', 'SCIENCE')),
    CONSTRAINT ck_lexical_sense_status CHECK (status IN (
        'DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_lexical_sense_entry_order ON lexical_sense (lexical_entry_id, display_order);

CREATE TABLE lexical_form (
    id                UUID         PRIMARY KEY,
    lexical_entry_id  UUID         NOT NULL REFERENCES lexical_entry (id),
    form_type         VARCHAR(32)  NOT NULL,
    original_form     VARCHAR(80)  NOT NULL,
    normalized_form   VARCHAR(80)  NOT NULL,
    notes             VARCHAR(300) NULL,
    status            VARCHAR(32)  NOT NULL,
    display_order     INTEGER      NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    created_by        UUID         NOT NULL,
    updated_by        UUID         NOT NULL,
    version           BIGINT       NOT NULL,
    CONSTRAINT ck_lexical_form_type CHECK (form_type IN (
        'VOCALIZED', 'PLURAL', 'SINGULAR', 'FEMININE', 'MASCULINE', 'ALTERNATE')),
    CONSTRAINT ck_lexical_form_status CHECK (status IN (
        'DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_lexical_form_entry ON lexical_form (lexical_entry_id, display_order);

CREATE TABLE source_citation (
    id              UUID          PRIMARY KEY,
    source_id       UUID          NOT NULL REFERENCES reference_source (id),
    page_from       INTEGER       NULL,
    page_to         INTEGER       NULL,
    volume          VARCHAR(40)   NULL,
    chapter         VARCHAR(120)  NULL,
    section_label   VARCHAR(120)  NULL,
    entry_label     VARCHAR(120)  NULL,
    source_locator  VARCHAR(200)  NULL,
    quoted_text     VARCHAR(2000) NULL,
    notes           VARCHAR(500)  NULL,
    created_at      TIMESTAMPTZ   NOT NULL,
    created_by      UUID          NOT NULL,
    CONSTRAINT ck_source_citation_pages CHECK (
        (page_from IS NULL OR page_from >= 1)
        AND (page_to IS NULL OR page_to >= 1)
        AND (page_from IS NULL OR page_to IS NULL OR page_from <= page_to))
);

CREATE INDEX ix_source_citation_source ON source_citation (source_id);

CREATE TABLE linguistic_relation (
    id                  UUID        PRIMARY KEY,
    relation_type       VARCHAR(32) NOT NULL,
    source_entry_id     UUID        NOT NULL REFERENCES lexical_entry (id),
    target_entry_id     UUID        NOT NULL REFERENCES lexical_entry (id),
    source_sense_id     UUID        NULL REFERENCES lexical_sense (id),
    target_sense_id     UUID        NULL REFERENCES lexical_sense (id),
    verification_level  VARCHAR(32) NOT NULL,
    status              VARCHAR(32) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,
    created_by          UUID        NOT NULL,
    updated_by          UUID        NOT NULL,
    version             BIGINT      NOT NULL,
    CONSTRAINT ck_linguistic_relation_distinct CHECK (source_entry_id <> target_entry_id),
    CONSTRAINT ck_linguistic_relation_type CHECK (relation_type IN ('SYNONYM', 'ANTONYM', 'RELATED', 'DERIVED_FROM')),
    CONSTRAINT ck_linguistic_relation_level CHECK (verification_level IN ('REPORTED', 'REVIEWED')),
    CONSTRAINT ck_linguistic_relation_shape CHECK (
        (relation_type = 'DERIVED_FROM' AND source_sense_id IS NULL AND target_sense_id IS NULL)
        OR (relation_type IN ('SYNONYM', 'ANTONYM', 'RELATED')
            AND source_sense_id IS NOT NULL
            AND target_sense_id IS NOT NULL
            AND source_sense_id <> target_sense_id)),
    CONSTRAINT ck_linguistic_relation_status CHECK (status IN (
        'DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE UNIQUE INDEX uq_linguistic_relation_senses ON linguistic_relation (
    relation_type,
    LEAST(source_sense_id, target_sense_id),
    GREATEST(source_sense_id, target_sense_id)
) WHERE source_sense_id IS NOT NULL;

CREATE UNIQUE INDEX uq_linguistic_relation_derivation ON linguistic_relation (
    relation_type, source_entry_id, target_entry_id
) WHERE relation_type = 'DERIVED_FROM';

CREATE INDEX ix_linguistic_relation_source ON linguistic_relation (source_entry_id);
CREATE INDEX ix_linguistic_relation_target ON linguistic_relation (target_entry_id);

CREATE TABLE usage_example (
    id              UUID          PRIMARY KEY,
    sense_id        UUID          NOT NULL REFERENCES lexical_sense (id),
    example_kind    VARCHAR(32)   NOT NULL,
    text_original   VARCHAR(1000) NOT NULL,
    text_normalized VARCHAR(1000) NOT NULL,
    explanation     VARCHAR(1000) NULL,
    citation_id     UUID          NULL REFERENCES source_citation (id),
    status          VARCHAR(32)   NOT NULL,
    display_order   INTEGER       NOT NULL,
    created_at      TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    created_by      UUID          NOT NULL,
    updated_by      UUID          NOT NULL,
    version         BIGINT        NOT NULL,
    CONSTRAINT ck_usage_example_kind CHECK (example_kind IN ('EDITORIAL', 'QUOTED')),
    CONSTRAINT ck_usage_example_quote CHECK (example_kind <> 'QUOTED' OR citation_id IS NOT NULL),
    CONSTRAINT ck_usage_example_status CHECK (status IN (
        'DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_usage_example_sense ON usage_example (sense_id, display_order);

CREATE TABLE sense_citation (
    sense_id    UUID NOT NULL REFERENCES lexical_sense (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (sense_id, citation_id)
);

CREATE TABLE entry_citation (
    entry_id    UUID NOT NULL REFERENCES lexical_entry (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (entry_id, citation_id)
);

CREATE TABLE relation_citation (
    relation_id UUID NOT NULL REFERENCES linguistic_relation (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (relation_id, citation_id)
);

CREATE TABLE root_citation (
    root_id     UUID NOT NULL REFERENCES linguistic_root (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (root_id, citation_id)
);

CREATE TABLE content_revision (
    id               UUID         PRIMARY KEY,
    target_type      VARCHAR(40)  NOT NULL,
    target_id        UUID         NOT NULL,
    revision_number  INTEGER      NOT NULL,
    snapshot         JSONB        NOT NULL,
    actor_id         UUID         NOT NULL,
    change_reason    VARCHAR(500) NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_content_revision UNIQUE (target_type, target_id, revision_number)
);

CREATE INDEX ix_content_revision_target ON content_revision (target_type, target_id, revision_number DESC);

INSERT INTO admin_permission (code, description) VALUES
    ('dictionary.entry.view', 'View dictionary entries'),
    ('dictionary.entry.create', 'Create dictionary entries'),
    ('dictionary.entry.edit', 'Edit dictionary entries'),
    ('dictionary.entry.submit', 'Submit linguistic content for review'),
    ('dictionary.entry.review', 'Review linguistic content'),
    ('dictionary.entry.publish', 'Publish verified linguistic content'),
    ('dictionary.entry.archive', 'Archive published linguistic content'),
    ('dictionary.root.view', 'View roots'),
    ('dictionary.root.manage', 'Create and edit roots'),
    ('dictionary.sense.manage', 'Manage senses'),
    ('dictionary.relation.manage', 'Manage linguistic relations'),
    ('dictionary.example.manage', 'Manage usage examples'),
    ('citation.manage', 'Attach citations to linguistic records');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code
FROM admin_permission
WHERE code LIKE 'dictionary.%' OR code = 'citation.manage';

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'dictionary.entry.view'),
    ('a0000000-0000-4000-8000-000000000002', 'dictionary.root.view'),
    ('a0000000-0000-4000-8000-000000000003', 'dictionary.entry.view'),
    ('a0000000-0000-4000-8000-000000000003', 'dictionary.entry.create'),
    ('a0000000-0000-4000-8000-000000000003', 'dictionary.entry.edit'),
    ('a0000000-0000-4000-8000-000000000003', 'dictionary.entry.submit'),
    ('a0000000-0000-4000-8000-000000000003', 'dictionary.root.view'),
    ('a0000000-0000-4000-8000-000000000003', 'dictionary.root.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'dictionary.sense.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'dictionary.relation.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'dictionary.example.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'citation.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'source.manage'),
    ('a0000000-0000-4000-8000-000000000004', 'dictionary.entry.view'),
    ('a0000000-0000-4000-8000-000000000004', 'dictionary.entry.review'),
    ('a0000000-0000-4000-8000-000000000004', 'dictionary.root.view'),
    ('a0000000-0000-4000-8000-000000000005', 'dictionary.entry.view'),
    ('a0000000-0000-4000-8000-000000000005', 'dictionary.entry.publish'),
    ('a0000000-0000-4000-8000-000000000005', 'dictionary.entry.archive'),
    ('a0000000-0000-4000-8000-000000000005', 'dictionary.root.view');
