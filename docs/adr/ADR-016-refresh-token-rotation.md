# ADR-016 Refresh Token Rotation

## Status

Accepted

## Date

2026-10-03

## Context

A long-lived access token would be hard to revoke. A refresh token stored in the clear would be a permanent credential if the database leaked.

## Decision

Refresh tokens are 32 random bytes. The database stores only their SHA-256 hash, a family id, expiry, and revocation time. The default lifetime is 14 days.

Each refresh revokes the presented token and issues a new token in the same family. Presenting a revoked token revokes the whole family. That family revocation commits in its own transaction, so the rejected refresh cannot roll it back. Logout revokes every refresh token for that staff user and denylists the current access token.

Login and refresh are rate limited per client address in Redis. Failed passwords increment a database counter and lock the account temporarily. The error for a bad username and a bad password is the same: `INVALID_CREDENTIALS`. The last active platform owner is not locked by that counter, so a guessed username cannot disable the only owner. Rate limiting still applies.

## Consequences

Reuse detection treats a race or a stolen token as a family compromise. The caller must log in again. Public reads do not use this mechanism.
