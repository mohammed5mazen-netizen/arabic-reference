CREATE TABLE content_import_batch (
    id            UUID        PRIMARY KEY,
    source_id     UUID        NOT NULL REFERENCES reference_source (id),
    file_name     VARCHAR(255) NOT NULL,
    file_sha256   VARCHAR(64) NOT NULL,
    imported_at   TIMESTAMPTZ NOT NULL,
    records_read  INTEGER     NOT NULL,
    valid_count   INTEGER     NOT NULL,
    invalid_count INTEGER     NOT NULL,
    duplicate_count INTEGER   NOT NULL,
    new_count     INTEGER     NOT NULL,
    update_count  INTEGER     NOT NULL,
    created_by    UUID        NOT NULL,
    CONSTRAINT ck_content_import_batch_counts CHECK (
        records_read >= 0 AND valid_count >= 0 AND invalid_count >= 0
        AND duplicate_count >= 0 AND new_count >= 0 AND update_count >= 0)
);

CREATE INDEX ix_content_import_batch_source_time ON content_import_batch (source_id, imported_at DESC);

CREATE TABLE content_import_record (
    id                UUID        PRIMARY KEY,
    source_id         UUID        NOT NULL REFERENCES reference_source (id),
    source_record_key VARCHAR(160) NOT NULL,
    target_type       VARCHAR(40) NOT NULL,
    target_id         UUID        NOT NULL,
    citation_id       UUID        NOT NULL REFERENCES source_citation (id),
    content_sha256    VARCHAR(64) NOT NULL,
    last_batch_id     UUID        NOT NULL REFERENCES content_import_batch (id),
    imported_at       TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_content_import_record_source_key UNIQUE (source_id, source_record_key)
);

CREATE INDEX ix_content_import_record_target ON content_import_record (target_type, target_id);
