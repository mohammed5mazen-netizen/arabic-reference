# ADR-096: Learning editorial workflow

Status: Accepted

Date: 2026-10-09

## Context

A published lesson is public teaching material. A draft edit must not replace it in place, and the author must not be the only reviewer.

## Decision

Paths use the shared states: draft, in review, changes requested, verified, published, archived. Verify requires a different person from the creator and the submitter. Publish requires a different person from the reviewer. Publish writes `published_snapshot` and a revision row. A later edit reopens a draft and keeps the snapshot. Archive clears the snapshot and drops the search documents.

## Consequences

The public API reads the snapshot only. Staff with `learning.lesson.view` can preview the working copy, including correct options.
