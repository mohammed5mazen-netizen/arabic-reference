# ADR-056: Search index lifecycle

Status: Accepted

Date: 2026-10-05

## Context

A public result must follow publication, and a failed index write must not leave published content without a document or the reverse.

## Decision

Publish and archive update `SearchIndex` in the same database transaction, under advisory lock `54051`. Draft edits do not. Rebuild and repair replace the whole index from published snapshots. Rebuild is idempotent. A second rebuild while one is running returns 409. `indexVersion` is 1.

## Consequences

There is no outbox. The guarantee holds because the index is in the same PostgreSQL database. A future external engine will need a different delivery strategy.
