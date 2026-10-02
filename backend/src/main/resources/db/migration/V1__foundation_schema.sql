-- S0 schema marker.
-- Linguistic entities, user accounts, and editorial workflow tables are intentionally absent.

CREATE TABLE foundation_marker (
    id         UUID        PRIMARY KEY,
    marker_key VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_foundation_marker_key UNIQUE (marker_key),
    CONSTRAINT ck_foundation_marker_key_not_blank CHECK (length(btrim(marker_key)) > 0)
);

COMMENT ON TABLE foundation_marker IS
    'S0 schema presence marker. Not a linguistic entity and not a user account.';
