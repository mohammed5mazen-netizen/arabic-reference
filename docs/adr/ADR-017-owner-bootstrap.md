# ADR-017 Owner Bootstrap

## Status

Accepted

## Date

2026-10-03

## Context

The first platform owner cannot be invited by an existing administrator. A hardcoded owner in source control would be a shared secret.

## Decision

On startup, if no `PLATFORM_OWNER` assignment exists and all four `BOOTSTRAP_OWNER_*` variables are set, the application creates one active owner. The password must meet the password policy. The hash is stored. The password is not logged.

If any variable is missing, startup continues and no owner is created. If an owner already exists, startup does not reset the password. A PostgreSQL advisory lock makes the insert idempotent across overlapping starts.

At least one active platform owner must remain. The API refuses to deactivate that owner or remove the role. Concurrent attempts take a row lock on the owner role.

## Consequences

Recovery from a disabled or lost owner is an operational database task, not a silent restart. Do not put `BOOTSTRAP_OWNER_PASSWORD` in Git.
