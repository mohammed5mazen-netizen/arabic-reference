ALTER TABLE source_citation ADD COLUMN poem VARCHAR(120) NULL;
ALTER TABLE source_citation ADD COLUMN verse VARCHAR(40) NULL;

CREATE TABLE literary_era (
    id                  UUID          PRIMARY KEY,
    name_original       VARCHAR(160)  NOT NULL,
    name_normalized     VARCHAR(160)  NOT NULL,
    slug                VARCHAR(180)  NOT NULL,
    start_description   VARCHAR(300)  NULL,
    end_description     VARCHAR(300)  NULL,
    summary             VARCHAR(2000) NULL,
    historical_context  VARCHAR(4000) NULL,
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
    CONSTRAINT uq_literary_era_slug UNIQUE (slug),
    CONSTRAINT ck_literary_era_order CHECK (display_order >= 0),
    CONSTRAINT ck_literary_era_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE literary_genre (
    id                  UUID          PRIMARY KEY,
    name_original       VARCHAR(160)  NOT NULL,
    name_normalized     VARCHAR(160)  NOT NULL,
    slug                VARCHAR(180)  NOT NULL,
    description         VARCHAR(2000) NULL,
    status              VARCHAR(32)   NOT NULL,
    reviewed_by         UUID          NULL,
    change_reason       VARCHAR(500)  NULL,
    published_snapshot  JSONB         NULL,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    created_by          UUID          NOT NULL,
    updated_by          UUID          NOT NULL,
    version             BIGINT        NOT NULL,
    CONSTRAINT uq_literary_genre_slug UNIQUE (slug),
    CONSTRAINT ck_literary_genre_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE literary_school (
    id                  UUID          PRIMARY KEY,
    era_id              UUID          NULL REFERENCES literary_era (id),
    name_original       VARCHAR(160)  NOT NULL,
    name_normalized     VARCHAR(160)  NOT NULL,
    slug                VARCHAR(180)  NOT NULL,
    description         VARCHAR(4000) NULL,
    status              VARCHAR(32)   NOT NULL,
    reviewed_by         UUID          NULL,
    change_reason       VARCHAR(500)  NULL,
    published_snapshot  JSONB         NULL,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    created_by          UUID          NOT NULL,
    updated_by          UUID          NOT NULL,
    version             BIGINT        NOT NULL,
    CONSTRAINT uq_literary_school_slug UNIQUE (slug),
    CONSTRAINT ck_literary_school_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE literary_figure (
    id                   UUID          PRIMARY KEY,
    canonical_name       VARCHAR(160)  NOT NULL,
    normalized_name      VARCHAR(160)  NOT NULL,
    slug                 VARCHAR(180)  NOT NULL,
    biography_summary    VARCHAR(4000) NULL,
    birth_precision      VARCHAR(16)   NOT NULL,
    birth_calendar       VARCHAR(16)   NOT NULL,
    birth_year           INTEGER       NULL,
    birth_exact_date     DATE          NULL,
    birth_display        VARCHAR(80)   NULL,
    birth_circa          BOOLEAN       NOT NULL,
    death_precision      VARCHAR(16)   NOT NULL,
    death_calendar       VARCHAR(16)   NOT NULL,
    death_year           INTEGER       NULL,
    death_exact_date     DATE          NULL,
    death_display        VARCHAR(80)   NULL,
    death_circa          BOOLEAN       NOT NULL,
    status               VARCHAR(32)   NOT NULL,
    reviewed_by          UUID          NULL,
    change_reason        VARCHAR(500)  NULL,
    published_snapshot   JSONB         NULL,
    created_at           TIMESTAMPTZ   NOT NULL,
    updated_at           TIMESTAMPTZ   NOT NULL,
    created_by           UUID          NOT NULL,
    updated_by           UUID          NOT NULL,
    version              BIGINT        NOT NULL,
    CONSTRAINT uq_literary_figure_slug UNIQUE (slug),
    CONSTRAINT ck_literary_figure_birth_precision CHECK (birth_precision IN ('EXACT', 'YEAR', 'APPROXIMATE', 'UNKNOWN')),
    CONSTRAINT ck_literary_figure_birth_calendar CHECK (birth_calendar IN ('GREGORIAN', 'HIJRI', 'UNSPECIFIED')),
    CONSTRAINT ck_literary_figure_death_precision CHECK (death_precision IN ('EXACT', 'YEAR', 'APPROXIMATE', 'UNKNOWN')),
    CONSTRAINT ck_literary_figure_death_calendar CHECK (death_calendar IN ('GREGORIAN', 'HIJRI', 'UNSPECIFIED')),
    CONSTRAINT ck_literary_figure_birth_year CHECK (birth_year IS NULL OR birth_year BETWEEN 1 AND 2500),
    CONSTRAINT ck_literary_figure_death_year CHECK (death_year IS NULL OR death_year BETWEEN 1 AND 2500),
    CONSTRAINT ck_literary_figure_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE literary_figure_alias (
    id          UUID         PRIMARY KEY,
    figure_id   UUID         NOT NULL REFERENCES literary_figure (id),
    alias       VARCHAR(160) NOT NULL,
    normalized  VARCHAR(160) NOT NULL,
    kind        VARCHAR(16)  NOT NULL,
    CONSTRAINT uq_literary_figure_alias UNIQUE (figure_id, normalized),
    CONSTRAINT ck_literary_figure_alias_kind CHECK (kind IN ('NAME', 'LAQAB', 'KUNYA', 'NISBA', 'OTHER'))
);

CREATE TABLE literary_figure_role (
    figure_id UUID        NOT NULL REFERENCES literary_figure (id),
    role      VARCHAR(20) NOT NULL,
    PRIMARY KEY (figure_id, role),
    CONSTRAINT ck_literary_figure_role CHECK (role IN ('POET', 'WRITER', 'CRITIC', 'LINGUIST', 'SCHOLAR', 'PLAYWRIGHT', 'NOVELIST', 'OTHER'))
);

CREATE TABLE literary_figure_era (
    figure_id UUID NOT NULL REFERENCES literary_figure (id),
    era_id    UUID NOT NULL REFERENCES literary_era (id),
    PRIMARY KEY (figure_id, era_id)
);

CREATE TABLE literary_school_figure (
    school_id UUID NOT NULL REFERENCES literary_school (id),
    figure_id UUID NOT NULL REFERENCES literary_figure (id),
    PRIMARY KEY (school_id, figure_id)
);

CREATE TABLE literary_work (
    id                  UUID          PRIMARY KEY,
    title_original      VARCHAR(200)  NOT NULL,
    title_normalized    VARCHAR(200)  NOT NULL,
    slug                VARCHAR(220)  NOT NULL,
    description         VARCHAR(4000) NULL,
    language_code       VARCHAR(16)   NOT NULL,
    genre_id            UUID          NULL REFERENCES literary_genre (id),
    era_id              UUID          NULL REFERENCES literary_era (id),
    composition_display VARCHAR(200)  NULL,
    rights_status       VARCHAR(20)   NOT NULL,
    rights_note         VARCHAR(1000) NULL,
    attribution         VARCHAR(500)  NULL,
    status              VARCHAR(32)   NOT NULL,
    reviewed_by         UUID          NULL,
    change_reason       VARCHAR(500)  NULL,
    published_snapshot  JSONB         NULL,
    created_at          TIMESTAMPTZ   NOT NULL,
    updated_at          TIMESTAMPTZ   NOT NULL,
    created_by          UUID          NOT NULL,
    updated_by          UUID          NOT NULL,
    version             BIGINT        NOT NULL,
    CONSTRAINT uq_literary_work_slug UNIQUE (slug),
    CONSTRAINT ck_literary_work_rights CHECK (rights_status IN ('PUBLIC_DOMAIN', 'LICENSED', 'RESTRICTED', 'UNKNOWN')),
    CONSTRAINT ck_literary_work_status CHECK (status IN ('DRAFT', 'IN_REVIEW', 'CHANGES_REQUESTED', 'VERIFIED', 'PUBLISHED', 'ARCHIVED'))
);

CREATE TABLE literary_work_alias (
    id         UUID         PRIMARY KEY,
    work_id    UUID         NOT NULL REFERENCES literary_work (id),
    alias      VARCHAR(200) NOT NULL,
    normalized VARCHAR(200) NOT NULL,
    CONSTRAINT uq_literary_work_alias UNIQUE (work_id, normalized)
);

CREATE TABLE literary_work_figure (
    work_id    UUID         NOT NULL REFERENCES literary_work (id),
    figure_id  UUID         NOT NULL REFERENCES literary_figure (id),
    role_label VARCHAR(80)  NULL,
    PRIMARY KEY (work_id, figure_id)
);

CREATE TABLE literary_excerpt (
    id          UUID         PRIMARY KEY,
    work_id     UUID         NOT NULL REFERENCES literary_work (id),
    excerpt_text VARCHAR(2000) NOT NULL,
    citation_id UUID         NOT NULL REFERENCES source_citation (id),
    display_order INTEGER    NOT NULL,
    CONSTRAINT ck_literary_excerpt_order CHECK (display_order >= 0)
);

CREATE INDEX ix_literary_excerpt_work ON literary_excerpt (work_id, display_order);

CREATE TABLE literary_era_citation (
    owner_id UUID NOT NULL REFERENCES literary_era (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (owner_id, citation_id)
);

CREATE TABLE literary_genre_citation (
    owner_id UUID NOT NULL REFERENCES literary_genre (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (owner_id, citation_id)
);

CREATE TABLE literary_school_citation (
    owner_id UUID NOT NULL REFERENCES literary_school (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (owner_id, citation_id)
);

CREATE TABLE literary_figure_citation (
    owner_id UUID NOT NULL REFERENCES literary_figure (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (owner_id, citation_id)
);

CREATE TABLE literary_work_citation (
    owner_id UUID NOT NULL REFERENCES literary_work (id),
    citation_id UUID NOT NULL REFERENCES source_citation (id),
    PRIMARY KEY (owner_id, citation_id)
);

INSERT INTO admin_permission (code, description) VALUES
    ('literature.view', 'View literature records'),
    ('literature.figure.manage', 'Create and edit eras, figures, genres, and schools'),
    ('literature.work.manage', 'Create and edit literary works'),
    ('literature.rights.manage', 'Set work rights and excerpts'),
    ('literature.review', 'Review literature content'),
    ('literature.publish', 'Publish or archive verified literature content');

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code FROM admin_permission WHERE code LIKE 'literature.%';

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'literature.view'),
    ('a0000000-0000-4000-8000-000000000003', 'literature.view'),
    ('a0000000-0000-4000-8000-000000000003', 'literature.figure.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'literature.work.manage'),
    ('a0000000-0000-4000-8000-000000000003', 'literature.rights.manage'),
    ('a0000000-0000-4000-8000-000000000004', 'literature.view'),
    ('a0000000-0000-4000-8000-000000000004', 'literature.review'),
    ('a0000000-0000-4000-8000-000000000005', 'literature.view'),
    ('a0000000-0000-4000-8000-000000000005', 'literature.publish');
