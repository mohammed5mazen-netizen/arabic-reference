# ADR-094: Quiz attempt snapshot

Status: Accepted

Date: 2026-10-09

## Context

If a quiz changes while someone is answering, the result can no longer be explained from the questions they saw.

## Decision

Starting an attempt copies the published quiz JSON, including the correct flags and the content version, onto the attempt row. Submit scores that copy. The response token is 32 random bytes. Only its SHA-256 is stored. Expiry defaults to 60 minutes and is clamped to 30–120. The same idempotency key returns the stored result. A second key after submit conflicts. The row is locked for update.

## Consequences

Editing or republishing the quiz does not change an open attempt. New attempts use the snapshot that is public at start time.
