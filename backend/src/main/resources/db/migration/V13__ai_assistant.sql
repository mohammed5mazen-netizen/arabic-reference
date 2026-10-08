CREATE TABLE ai_usage (
    id              UUID         PRIMARY KEY,
    requested_at    TIMESTAMPTZ  NOT NULL,
    provider        VARCHAR(40)  NOT NULL,
    model           VARCHAR(80)  NOT NULL,
    status          VARCHAR(40)  NOT NULL,
    latency_ms      INTEGER      NOT NULL,
    input_tokens    INTEGER,
    output_tokens   INTEGER,
    evidence_count  INTEGER      NOT NULL,
    CONSTRAINT ck_ai_usage_status CHECK (status IN (
        'GROUNDED',
        'PARTIALLY_GROUNDED',
        'INSUFFICIENT_EVIDENCE',
        'PROVIDER_ERROR'
    ))
);

CREATE INDEX ix_ai_usage_requested_at ON ai_usage (requested_at);

INSERT INTO admin_permission (code, description) VALUES
    ('ai.admin.view', 'View linguistic assistant status and aggregate usage');

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000001', 'ai.admin.view'),
    ('a0000000-0000-4000-8000-000000000002', 'ai.admin.view'),
    ('a0000000-0000-4000-8000-000000000006', 'ai.admin.view');
