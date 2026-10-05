CREATE TABLE rhetoric_topic (
    id                  UUID          PRIMARY KEY,
    category            VARCHAR(20)   NOT NULL,
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
    CONSTRAINT uq_rhetoric_topic_slug UNIQUE (slug),
    CONSTRAINT ck_rhetoric_topic_category CHECK (category IN ('MAANI', 'BAYAN', 'BADI', 'OTHER')),
    CONSTRAINT ck_rhetoric_topic_order CHECK (display_order >= 0),
    CONSTRAINT ck_rhetoric_topic_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE rhetoric_device (
    id                    UUID          PRIMARY KEY,
    topic_id              UUID          NOT NULL REFERENCES rhetoric_topic (id),
    name_original         VARCHAR(160)  NOT NULL,
    name_normalized       VARCHAR(160)  NOT NULL,
    slug                  VARCHAR(180)  NOT NULL,
    short_definition      VARCHAR(500)  NOT NULL,
    detailed_explanation  VARCHAR(4000) NULL,
    status                VARCHAR(32)   NOT NULL,
    reviewed_by           UUID          NULL,
    change_reason         VARCHAR(500)  NULL,
    published_snapshot    JSONB         NULL,
    created_at            TIMESTAMPTZ   NOT NULL,
    updated_at            TIMESTAMPTZ   NOT NULL,
    created_by            UUID          NOT NULL,
    updated_by            UUID          NOT NULL,
    version               BIGINT        NOT NULL,
    CONSTRAINT uq_rhetoric_device_slug UNIQUE (slug),
    CONSTRAINT ck_rhetoric_device_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE INDEX ix_rhetoric_device_topic ON rhetoric_device (topic_id);

CREATE TABLE rhetoric_component (
    id            UUID          PRIMARY KEY,
    device_id     UUID          NOT NULL REFERENCES rhetoric_device (id),
    kind          VARCHAR(20)   NOT NULL,
    heading       VARCHAR(160)  NOT NULL,
    body          VARCHAR(4000) NOT NULL,
    display_order INTEGER       NOT NULL,
    CONSTRAINT ck_rhetoric_component_kind CHECK (kind IN ('DEFINITION', 'CHARACTERISTIC', 'TYPE', 'CONDITION', 'DIFFERENCE', 'NOTE')),
    CONSTRAINT ck_rhetoric_component_order CHECK (display_order >= 0)
);

CREATE INDEX ix_rhetoric_component_device ON rhetoric_component (device_id, display_order);

CREATE TABLE rhetoric_example (
    id                       UUID          PRIMARY KEY,
    device_id                UUID          NOT NULL REFERENCES rhetoric_device (id),
    kind                     VARCHAR(20)   NOT NULL,
    example_text             VARCHAR(2000) NOT NULL,
    explanation              VARCHAR(2000) NOT NULL,
    highlighted_segment      VARCHAR(300)  NULL,
    interpretation           VARCHAR(2000) NULL,
    scholarly_note           VARCHAR(2000) NULL,
    alternative_interpretation VARCHAR(2000) NULL,
    citation_id              UUID          NULL REFERENCES source_citation (id),
    display_order            INTEGER       NOT NULL,
    CONSTRAINT ck_rhetoric_example_kind CHECK (kind IN ('QUOTED', 'CONSTRUCTED')),
    CONSTRAINT ck_rhetoric_example_order CHECK (display_order >= 0)
);

CREATE INDEX ix_rhetoric_example_device ON rhetoric_example (device_id, display_order);

CREATE TABLE rhetoric_relation (
    id               UUID        PRIMARY KEY,
    source_device_id UUID        NOT NULL REFERENCES rhetoric_device (id),
    target_device_id UUID        NOT NULL REFERENCES rhetoric_device (id),
    kind             VARCHAR(32) NOT NULL,
    CONSTRAINT uq_rhetoric_relation UNIQUE (source_device_id, target_device_id, kind),
    CONSTRAINT ck_rhetoric_relation_distinct CHECK (source_device_id <> target_device_id),
    CONSTRAINT ck_rhetoric_relation_kind CHECK (kind IN ('RELATED_TO', 'CONTRASTS_WITH', 'TYPE_OF', 'OFTEN_CONFUSED_WITH', 'SEE_ALSO'))
);

CREATE TABLE rhetoric_topic_citation (
    owner_id    UUID NOT NULL REFERENCES rhetoric_topic (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (owner_id, citation_id)
);

CREATE TABLE rhetoric_device_citation (
    owner_id    UUID NOT NULL REFERENCES rhetoric_device (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (owner_id, citation_id)
);

INSERT INTO admin_permission (code, description) VALUES
    ('rhetoric.topic.view', 'View rhetoric topics and devices'),
    ('rhetoric.topic.manage', 'Create and edit rhetoric topics'),
    ('rhetoric.device.create', 'Create a rhetoric device'),
    ('rhetoric.device.edit', 'Edit a rhetoric device, its examples, and its relations'),
    ('rhetoric.device.review', 'Review rhetoric content'),
    ('rhetoric.device.publish', 'Publish or archive verified rhetoric content');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code FROM admin_permission WHERE code LIKE 'rhetoric.%';

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'rhetoric.topic.view'),
    ('a0000000-0000-4000-8000-000000000003', 'rhetoric.topic.view'),
    ('a0000000-0000-4000-8000-000000000003', 'rhetoric.topic.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'rhetoric.device.create'),
    ('a0000000-0000-4000-8000-000000000003', 'rhetoric.device.edit'),
    ('a0000000-0000-4000-8000-000000000004', 'rhetoric.topic.view'),
    ('a0000000-0000-4000-8000-000000000004', 'rhetoric.device.review'),
    ('a0000000-0000-4000-8000-000000000005', 'rhetoric.topic.view'),
    ('a0000000-0000-4000-8000-000000000005', 'rhetoric.device.publish');
