-- Editorial staff identity. These tables are not visitor accounts.
-- Linguistic content tables stay out of S1.

CREATE TABLE admin_user (
    id                     UUID         PRIMARY KEY,
    username               VARCHAR(32)  NOT NULL,
    email                  VARCHAR(320) NOT NULL,
    display_name           VARCHAR(120) NOT NULL,
    password_hash          VARCHAR(255) NOT NULL,
    status                 VARCHAR(16)  NOT NULL,
    must_change_password   BOOLEAN      NOT NULL,
    failed_login_attempts  INTEGER      NOT NULL,
    locked_until           TIMESTAMPTZ  NULL,
    last_login_at          TIMESTAMPTZ  NULL,
    password_changed_at    TIMESTAMPTZ  NOT NULL,
    created_at             TIMESTAMPTZ  NOT NULL,
    updated_at             TIMESTAMPTZ  NOT NULL,
    version                BIGINT       NOT NULL,
    CONSTRAINT uq_admin_user_username UNIQUE (username),
    CONSTRAINT uq_admin_user_email UNIQUE (email),
    CONSTRAINT ck_admin_user_status CHECK (status IN ('ACTIVE', 'DISABLED', 'LOCKED')),
    CONSTRAINT ck_admin_user_username CHECK (username = lower(username) AND username ~ '^[a-z0-9][a-z0-9._-]{2,31}$'),
    CONSTRAINT ck_admin_user_email CHECK (email = lower(email) AND position('@' IN email) > 1),
    CONSTRAINT ck_admin_user_failed CHECK (failed_login_attempts >= 0),
    CONSTRAINT ck_admin_user_version CHECK (version >= 0),
    CONSTRAINT ck_admin_user_password_hash CHECK (length(password_hash) >= 20)
);

CREATE INDEX ix_admin_user_status ON admin_user (status);

CREATE TABLE admin_role (
    id           UUID         PRIMARY KEY,
    code         VARCHAR(64)  NOT NULL,
    name         VARCHAR(120) NOT NULL,
    description  VARCHAR(500) NOT NULL,
    system_role  BOOLEAN      NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL,
    updated_at   TIMESTAMPTZ  NOT NULL,
    version      BIGINT       NOT NULL,
    CONSTRAINT uq_admin_role_code UNIQUE (code),
    CONSTRAINT ck_admin_role_code CHECK (code = upper(code) AND code ~ '^[A-Z][A-Z0-9_]{1,63}$'),
    CONSTRAINT ck_admin_role_version CHECK (version >= 0)
);

CREATE TABLE admin_permission (
    code         VARCHAR(80)  PRIMARY KEY,
    description  VARCHAR(240) NOT NULL,
    CONSTRAINT ck_admin_permission_code CHECK (code ~ '^[a-z]+(\.[a-z_]+)+$')
);

CREATE TABLE admin_role_permission (
    role_id          UUID        NOT NULL REFERENCES admin_role (id),
    permission_code  VARCHAR(80) NOT NULL REFERENCES admin_permission (code),
    PRIMARY KEY (role_id, permission_code)
);

CREATE TABLE admin_user_role (
    user_id      UUID        NOT NULL REFERENCES admin_user (id),
    role_id      UUID        NOT NULL REFERENCES admin_role (id),
    assigned_at  TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE admin_refresh_token (
    id           UUID        PRIMARY KEY,
    user_id      UUID        NOT NULL REFERENCES admin_user (id),
    family_id    UUID        NOT NULL,
    token_hash   VARCHAR(64) NOT NULL,
    expires_at   TIMESTAMPTZ NOT NULL,
    revoked_at   TIMESTAMPTZ NULL,
    replaced_by  UUID        NULL REFERENCES admin_refresh_token (id),
    created_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_admin_refresh_token_hash UNIQUE (token_hash)
);

CREATE INDEX ix_admin_refresh_token_family ON admin_refresh_token (family_id);
CREATE INDEX ix_admin_refresh_token_user ON admin_refresh_token (user_id);

CREATE TABLE admin_audit_event (
    id           UUID         PRIMARY KEY,
    actor_id     UUID         NULL,
    event_type   VARCHAR(64)  NOT NULL,
    target_type  VARCHAR(64)  NULL,
    target_id    VARCHAR(64)  NULL,
    occurred_at  TIMESTAMPTZ  NOT NULL,
    trace_id     VARCHAR(64)  NOT NULL,
    metadata     JSONB        NOT NULL,
    CONSTRAINT ck_admin_audit_event_type CHECK (event_type IN (
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
        'ADMIN_ROLE_REMOVED'
    ))
);

CREATE INDEX ix_admin_audit_event_occurred ON admin_audit_event (occurred_at DESC);

CREATE FUNCTION admin_audit_reject_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'audit records are append-only';
END;
$$;

CREATE TRIGGER trg_admin_audit_append_only
    BEFORE UPDATE OR DELETE ON admin_audit_event
    FOR EACH ROW
    EXECUTE FUNCTION admin_audit_reject_mutation();

INSERT INTO admin_permission (code, description) VALUES
    ('admin.user.view', 'View editorial accounts'),
    ('admin.user.create', 'Create editorial accounts'),
    ('admin.user.edit', 'Edit editorial account profile'),
    ('admin.user.activate', 'Activate an editorial account'),
    ('admin.user.deactivate', 'Deactivate an editorial account'),
    ('admin.user.unlock', 'Unlock an editorial account'),
    ('admin.user.reset_password', 'Set a temporary editorial password'),
    ('admin.role.view', 'View roles'),
    ('admin.role.create', 'Create custom roles'),
    ('admin.role.edit', 'Edit roles'),
    ('admin.role.assign', 'Assign roles to editorial accounts'),
    ('admin.permission.view', 'View the permission catalog'),
    ('admin.audit.view', 'Read the audit trail'),
    ('editorial.content.create', 'Create linguistic content later'),
    ('editorial.content.edit', 'Edit linguistic content later'),
    ('editorial.content.submit', 'Submit content for review later'),
    ('editorial.content.review', 'Review linguistic content later'),
    ('editorial.content.approve', 'Approve reviewed content later'),
    ('editorial.content.publish', 'Publish approved content later'),
    ('editorial.content.archive', 'Archive published content later'),
    ('source.view', 'View linguistic sources later'),
    ('source.manage', 'Manage linguistic sources later');

INSERT INTO admin_role (id, code, name, description, system_role, created_at, updated_at, version) VALUES
    ('a0000000-0000-4000-8000-000000000001', 'PLATFORM_OWNER', 'مالك المنصة', 'Full editorial authority. At least one active owner must remain.', TRUE, TIMESTAMPTZ '2026-10-02T21:00:00Z', TIMESTAMPTZ '2026-10-02T21:00:00Z', 0),
    ('a0000000-0000-4000-8000-000000000002', 'ADMIN', 'مدير النظام', 'Manages editorial accounts and roles within held permissions.', TRUE, TIMESTAMPTZ '2026-10-02T21:00:00Z', TIMESTAMPTZ '2026-10-02T21:00:00Z', 0),
    ('a0000000-0000-4000-8000-000000000003', 'EDITOR', 'محرر', 'Drafts linguistic content in later stages.', TRUE, TIMESTAMPTZ '2026-10-02T21:00:00Z', TIMESTAMPTZ '2026-10-02T21:00:00Z', 0),
    ('a0000000-0000-4000-8000-000000000004', 'LINGUISTIC_REVIEWER', 'مراجع لغوي', 'Reviews linguistic content in later stages.', TRUE, TIMESTAMPTZ '2026-10-02T21:00:00Z', TIMESTAMPTZ '2026-10-02T21:00:00Z', 0),
    ('a0000000-0000-4000-8000-000000000005', 'PUBLISHER', 'ناشر', 'Approves and publishes content in later stages.', TRUE, TIMESTAMPTZ '2026-10-02T21:00:00Z', TIMESTAMPTZ '2026-10-02T21:00:00Z', 0),
    ('a0000000-0000-4000-8000-000000000006', 'AUDITOR', 'مدقق', 'Reads the audit trail without changing content.', TRUE, TIMESTAMPTZ '2026-10-02T21:00:00Z', TIMESTAMPTZ '2026-10-02T21:00:00Z', 0);

INSERT INTO admin_role_permission (role_id, permission_code)
SELECT 'a0000000-0000-4000-8000-000000000001', code FROM admin_permission;

INSERT INTO admin_role_permission (role_id, permission_code) VALUES
    ('a0000000-0000-4000-8000-000000000002', 'admin.user.view'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.user.create'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.user.edit'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.user.activate'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.user.deactivate'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.user.unlock'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.user.reset_password'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.role.view'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.role.create'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.role.edit'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.role.assign'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.permission.view'),
    ('a0000000-0000-4000-8000-000000000002', 'admin.audit.view'),
    ('a0000000-0000-4000-8000-000000000002', 'source.view'),
    ('a0000000-0000-4000-8000-000000000003', 'editorial.content.create'),
    ('a0000000-0000-4000-8000-000000000003', 'editorial.content.edit'),
    ('a0000000-0000-4000-8000-000000000003', 'editorial.content.submit'),
    ('a0000000-0000-4000-8000-000000000003', 'source.view'),
    ('a0000000-0000-4000-8000-000000000004', 'editorial.content.review'),
    ('a0000000-0000-4000-8000-000000000004', 'source.view'),
    ('a0000000-0000-4000-8000-000000000005', 'editorial.content.approve'),
    ('a0000000-0000-4000-8000-000000000005', 'editorial.content.publish'),
    ('a0000000-0000-4000-8000-000000000005', 'editorial.content.archive'),
    ('a0000000-0000-4000-8000-000000000005', 'source.view'),
    ('a0000000-0000-4000-8000-000000000006', 'admin.audit.view'),
    ('a0000000-0000-4000-8000-000000000006', 'source.view');
