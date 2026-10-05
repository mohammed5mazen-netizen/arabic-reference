# ADR-066: S6 editorial workflow reuse

Status: Accepted

Date: 2026-10-05

## Context

Spelling, rhetoric, literature, and articles need the same review life cycle as the dictionary and grammar, without a second workflow engine.

## Decision

S6 records use `DRAFT`, `IN_REVIEW`, `CHANGES_REQUESTED`, `VERIFIED`, `PUBLISHED`, and `ARCHIVED` through `EditorialStore`. The creator cannot review or publish their own record. The reviewer cannot publish it. Grammar's own editorial record is left as it was.

## Consequences

Optimistic locking stays on `@Version`. A stale write returns 409. New permissions are granted to the existing editor, reviewer, and publisher roles.
