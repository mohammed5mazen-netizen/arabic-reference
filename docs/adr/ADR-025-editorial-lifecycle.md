# ADR-025 Editorial Lifecycle

## Status

Accepted

## Date

2026-10-03

## Context

Linguistic content needs review before it is public, and the creator must not approve their own work.

## Decision

Working states are `DRAFT`, `IN_REVIEW`, `CHANGES_REQUESTED`, `VERIFIED`, `PUBLISHED`, and `ARCHIVED`.

Editor submit moves draft or returned content to review. A reviewer verifies or returns it with a reason, and the reviewer is not the creator. A publisher publishes only `VERIFIED` content, and the publisher is neither the creator nor the reviewer. Archive is a separate permission from `PUBLISHED`. `DRAFT` cannot jump to `PUBLISHED`.

`CHANGES_REQUESTED` is a real state so a return is not another draft and is audited as `CONTENT_CHANGES_REQUESTED`.

The public site shows a record only when `published_snapshot` is present and the status is not `ARCHIVED`. Editing a published record opens a new draft and keeps the previous snapshot public.

## Consequences

Four-eyes applies to entries, roots, and sources through the same transition permissions. Roots and sources are created with their own manage permissions.
