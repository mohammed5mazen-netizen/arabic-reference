# ADR-118: Safe bulk editorial operations

Status: Accepted

Date: 2026-10-09

## Context

Selecting many records is useful for assignment and quality checks. The same endpoint must not become a way to publish or delete without review.

## Decision

Bulk accepts `ASSIGN_REVIEWER` and `QUALITY_CHECK` only, with a configurable limit of at most 100. `PUBLISH`, `DELETE`, and `VERIFY` return 403. Each item reports success or failure. Four-eyes and permissions still apply to every assignment.

## Consequences

A mixed batch can partially succeed. Independent items are not rolled back together.
