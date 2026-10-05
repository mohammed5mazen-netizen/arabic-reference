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
    'SEARCH_INDEX_REPAIR'
));

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE search_document (
    id               UUID         PRIMARY KEY,
    entity_type      VARCHAR(40)  NOT NULL,
    entity_id        UUID         NOT NULL,
    title_original   VARCHAR(200) NOT NULL,
    title_normalized VARCHAR(200) NOT NULL,
    search_key       VARCHAR(200) NOT NULL,
    searchable_text  TEXT         NOT NULL,
    root_normalized  VARCHAR(64)  NULL,
    root_label       VARCHAR(64)  NULL,
    part_of_speech   VARCHAR(40)  NULL,
    category         VARCHAR(40)  NULL,
    subtitle         VARCHAR(300) NULL,
    snippet          TEXT         NOT NULL,
    url_path         VARCHAR(300) NOT NULL,
    published_at     TIMESTAMPTZ  NULL,
    related_count    INTEGER      NOT NULL DEFAULT 0,
    popularity       INTEGER      NOT NULL DEFAULT 0,
    source_quality   INTEGER      NOT NULL DEFAULT 0,
    index_version    INTEGER      NOT NULL,
    CONSTRAINT uq_search_document_entity UNIQUE (entity_type, entity_id),
    CONSTRAINT ck_search_document_type CHECK (entity_type IN ('DICTIONARY_ENTRY', 'ROOT', 'GRAMMAR_TOPIC', 'GRAMMAR_RULE', 'GRAMMAR_CONCEPT'))
);

CREATE TABLE search_document_token (
    id             UUID         PRIMARY KEY,
    document_id    UUID         NOT NULL REFERENCES search_document (id) ON DELETE CASCADE,
    token_original VARCHAR(200) NOT NULL,
    token_key      VARCHAR(200) NOT NULL,
    kind           VARCHAR(16)  NOT NULL,
    CONSTRAINT ck_search_token_kind CHECK (kind IN ('ALIAS', 'FORM'))
);

CREATE INDEX ix_search_token_key ON search_document_token (token_key);
CREATE INDEX ix_search_token_document ON search_document_token (document_id);
CREATE INDEX ix_search_document_key ON search_document (search_key);
CREATE INDEX ix_search_document_type ON search_document (entity_type);
CREATE INDEX ix_search_document_root ON search_document (root_normalized);
CREATE INDEX ix_search_document_pos ON search_document (part_of_speech);
CREATE INDEX ix_search_document_key_trgm ON search_document USING gin (search_key gin_trgm_ops);
CREATE INDEX ix_search_document_text_trgm ON search_document USING gin (searchable_text gin_trgm_ops);

CREATE TABLE search_index_state (
    id              SMALLINT PRIMARY KEY,
    index_version   INTEGER NOT NULL,
    document_count  INTEGER NOT NULL,
    last_rebuild_at TIMESTAMPTZ NULL,
    last_rebuild_by UUID NULL,
    CONSTRAINT ck_search_index_state_singleton CHECK (id = 1)
);

INSERT INTO search_index_state (id, index_version, document_count) VALUES (1, 1, 0);

INSERT INTO admin_permission (code, description) VALUES
    ('search.admin.view', 'View search index status'),
    ('search.reindex', 'Rebuild the published search index');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code
FROM admin_permission
WHERE code LIKE 'search.%';

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'search.admin.view'),
    ('a0000000-0000-4000-8000-000000000002', 'search.reindex'),
    ('a0000000-0000-4000-8000-000000000003', 'search.admin.view'),
    ('a0000000-0000-4000-8000-000000000004', 'search.admin.view'),
    ('a0000000-0000-4000-8000-000000000005', 'search.admin.view');
