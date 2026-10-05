# ADR-068: S6 published snapshot strategy

Status: Accepted

Date: 2026-10-05

## Context

A correction to a published page must not change the public page before review.

## Decision

Each S6 aggregate keeps a `published_snapshot`. Editing a published row records a `content_revision` and returns the row to draft. Public queries and the search index keep the snapshot until the next publication replaces it. Archive hides the row and removes its search document.

## Consequences

Public pages do not join live child tables for the reading view. The snapshot is the page.
