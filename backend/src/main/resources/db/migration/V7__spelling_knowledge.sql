CREATE TABLE spelling_topic (
    id                  UUID          PRIMARY KEY,
    title_original      VARCHAR(160)  NOT NULL,
    title_normalized    VARCHAR(160)  NOT NULL,
    slug                VARCHAR(180)  NOT NULL,
    summary             VARCHAR(1000) NULL,
    display_order       INTEGER       NOT NULL,
    status              VARCHAR(32)   NOT NULL,
    reviewed_by         UUID          NULL,
    change_reason       VARCHAR(500)  NULL,
    published_snapshot  JSONB         NULL,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    created_by          UUID          NOT NULL,
    updated_by          UUID          NOT NULL,
    version             BIGINT        NOT NULL,
    CONSTRAINT uq_spelling_topic_slug UNIQUE (slug),
    CONSTRAINT ck_spelling_topic_order CHECK (display_order >= 0),
    CONSTRAINT ck_spelling_topic_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE spelling_rule (
    id                  UUID          PRIMARY KEY,
    topic_id            UUID          NOT NULL REFERENCES spelling_topic (id),
    title_original      VARCHAR(160)  NOT NULL,
    title_normalized    VARCHAR(160)  NOT NULL,
    slug                VARCHAR(180)  NOT NULL,
    summary             VARCHAR(1000) NULL,
    core_rule           VARCHAR(4000) NOT NULL,
    difficulty          VARCHAR(20)   NULL,
    status              VARCHAR(32)   NOT NULL,
    reviewed_by         UUID          NULL,
    change_reason       VARCHAR(500)  NULL,
    published_snapshot  JSONB         NULL,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    created_by          UUID          NOT NULL,
    updated_by          UUID          NOT NULL,
    version             BIGINT        NOT NULL,
    CONSTRAINT uq_spelling_rule_slug UNIQUE (slug),
    CONSTRAINT ck_spelling_rule_difficulty CHECK (difficulty IS NULL OR difficulty IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED')),
    CONSTRAINT ck_spelling_rule_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_spelling_rule_topic ON spelling_rule (topic_id);

CREATE TABLE spelling_clause (
    id            UUID          PRIMARY KEY,
    rule_id       UUID          NOT NULL REFERENCES spelling_rule (id),
    kind          VARCHAR(20)   NOT NULL,
    heading       VARCHAR(160)  NOT NULL,
    body          VARCHAR(4000) NOT NULL,
    display_order INTEGER       NOT NULL,
    CONSTRAINT ck_spelling_clause_kind CHECK (kind IN ('DEFINITION', 'CONDITION', 'EXCEPTION', 'NOTE')),
    CONSTRAINT ck_spelling_clause_order CHECK (display_order >= 0)
);

CREATE INDEX ix_spelling_clause_rule ON spelling_clause (rule_id, display_order);

CREATE TABLE spelling_example (
    id              UUID          PRIMARY KEY,
    rule_id         UUID          NOT NULL REFERENCES spelling_rule (id),
    kind            VARCHAR(20)   NOT NULL,
    correct_form    VARCHAR(300)  NULL,
    incorrect_form  VARCHAR(300)  NULL,
    explanation     VARCHAR(1000) NULL,
    context_note    VARCHAR(500)  NULL,
    common_form     VARCHAR(300)  NULL,
    reason          VARCHAR(1000) NULL,
    citation_id     UUID          NULL REFERENCES source_citation (id),
    display_order   INTEGER       NOT NULL,
    CONSTRAINT ck_spelling_example_kind CHECK (kind IN ('CONTRAST', 'QUOTED', 'CONSTRUCTED', 'COMMON_MISTAKE')),
    CONSTRAINT ck_spelling_example_order CHECK (display_order >= 0)
);

CREATE INDEX ix_spelling_example_rule ON spelling_example (rule_id, display_order);

CREATE TABLE spelling_topic_citation (
    owner_id    UUID NOT NULL REFERENCES spelling_topic (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (owner_id, citation_id)
);

CREATE TABLE spelling_rule_citation (
    owner_id    UUID NOT NULL REFERENCES spelling_rule (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (owner_id, citation_id)
);

INSERT INTO admin_permission (code, description) VALUES
    ('spelling.topic.view', 'View spelling topics and rules'),
    ('spelling.topic.manage', 'Create and edit spelling topics'),
    ('spelling.rule.create', 'Create a spelling rule'),
    ('spelling.rule.edit', 'Edit a spelling rule, its clauses, and its examples'),
    ('spelling.rule.review', 'Review spelling content'),
    ('spelling.rule.publish', 'Publish or archive verified spelling content');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code FROM admin_permission WHERE code LIKE 'spelling.%';

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'spelling.topic.view'),
    ('a0000000-0000-4000-8000-000000000003', 'spelling.topic.view'),
    ('a0000000-0000-4000-8000-000000000003', 'spelling.topic.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'spelling.rule.create'),
    ('a0000000-0000-4000-8000-000000000003', 'spelling.rule.edit'),
    ('a0000000-0000-4000-8000-000000000004', 'spelling.topic.view'),
    ('a0000000-0000-4000-8000-000000000004', 'spelling.rule.review'),
    ('a0000000-0000-4000-8000-000000000005', 'spelling.topic.view'),
    ('a0000000-0000-4000-8000-000000000005', 'spelling.rule.publish');
