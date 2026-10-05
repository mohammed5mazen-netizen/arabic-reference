CREATE TABLE article (
    id                  UUID          PRIMARY KEY,
    title_original      VARCHAR(200)  NOT NULL,
    title_normalized    VARCHAR(200)  NOT NULL,
    slug                VARCHAR(220)  NOT NULL,
    excerpt             VARCHAR(500)  NOT NULL,
    article_type        VARCHAR(32)   NOT NULL,
    cover_label         VARCHAR(160)  NULL,
    editor_name         VARCHAR(160)  NULL,
    status              VARCHAR(32)   NOT NULL,
    reviewed_by         UUID          NULL,
    change_reason       VARCHAR(500)  NULL,
    published_snapshot  JSONB         NULL,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    created_by          UUID          NOT NULL,
    updated_by          UUID          NOT NULL,
    version             BIGINT        NOT NULL,
    CONSTRAINT uq_article_slug UNIQUE (slug),
    CONSTRAINT ck_article_type CHECK (article_type IN ('LINGUISTIC', 'EDUCATIONAL', 'REFERENCE', 'HISTORY_OF_ARABIC', 'TERMINOLOGY', 'EDITORIAL')),
    CONSTRAINT ck_article_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE article_section (
    id            UUID          PRIMARY KEY,
    article_id    UUID          NOT NULL REFERENCES article (id),
    heading       VARCHAR(200)  NOT NULL,
    body          VARCHAR(8000) NOT NULL,
    display_order INTEGER       NOT NULL,
    CONSTRAINT ck_article_section_order CHECK (display_order >= 0)
);

CREATE INDEX ix_article_section_article ON article_section (article_id, display_order);

CREATE TABLE article_tag (
    id         UUID         PRIMARY KEY,
    name       VARCHAR(80)  NOT NULL,
    normalized VARCHAR(80)  NOT NULL,
    CONSTRAINT uq_article_tag_normalized UNIQUE (normalized)
);

CREATE TABLE article_tag_link (
    article_id UUID NOT NULL REFERENCES article (id),
    tag_id     UUID NOT NULL REFERENCES article_tag (id),
    PRIMARY KEY (article_id, tag_id)
);

CREATE TABLE article_citation (
    id          UUID NOT NULL PRIMARY KEY,
    article_id  UUID NOT NULL REFERENCES article (id),
    section_id  UUID NULL REFERENCES article_section (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    CONSTRAINT uq_article_citation UNIQUE (article_id, section_id, citation_id)
);

CREATE UNIQUE INDEX uq_article_citation_article_level
    ON article_citation (article_id, citation_id)
    WHERE section_id IS NULL;

-- No foreign keys: the target lives in another module. Application code checks the published target.
CREATE TABLE knowledge_relation (
    id          UUID        PRIMARY KEY,
    owner_type  VARCHAR(32) NOT NULL,
    owner_id    UUID        NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    target_id   UUID        NOT NULL,
    CONSTRAINT uq_knowledge_relation UNIQUE (owner_type, owner_id, target_type, target_id),
    CONSTRAINT ck_knowledge_relation_owner CHECK (owner_type IN ('ARTICLE')),
    CONSTRAINT ck_knowledge_relation_target CHECK (target_type IN (
        'DICTIONARY_ENTRY', 'ROOT', 'GRAMMAR_RULE', 'SPELLING_RULE', 'RHETORIC_DEVICE', 'LITERARY_FIGURE', 'LITERARY_WORK'))
);

CREATE INDEX ix_knowledge_relation_owner ON knowledge_relation (owner_type, owner_id);

INSERT INTO admin_permission (code, description) VALUES
    ('content.article.view', 'View articles'),
    ('content.article.create', 'Create an article'),
    ('content.article.edit', 'Edit article sections, tags, citations, and relations'),
    ('content.article.review', 'Review articles'),
    ('content.article.publish', 'Publish or archive articles');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code FROM admin_permission WHERE code LIKE 'content.article.%';

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'content.article.view'),
    ('a0000000-0000-4000-8000-000000000003', 'content.article.view'),
    ('a0000000-0000-4000-8000-000000000003', 'content.article.create'),
    ('a0000000-0000-4000-8000-000000000003', 'content.article.edit'),
    ('a0000000-0000-4000-8000-000000000004', 'content.article.view'),
    ('a0000000-0000-4000-8000-000000000004', 'content.article.review'),
    ('a0000000-0000-4000-8000-000000000005', 'content.article.view'),
    ('a0000000-0000-4000-8000-000000000005', 'content.article.publish');
