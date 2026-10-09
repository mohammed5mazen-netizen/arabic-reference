# ADR-113: Editorial diff strategy

Status: Accepted

Date: 2026-10-09

## Context

Review needs the difference between a published snapshot and the current draft, or between two stored revisions, including Arabic vowels.

## Decision

The diff request supplies both revision numbers or neither. Neither means published snapshot versus current title, slug, status, and summary. Known fields get Arabic change labels. Other safe fields get a field-level diff. Comparison uses grapheme clusters so a combining mark is not split from its letter. Secret and answer fields are dropped before comparison.

## Consequences

Domains that do not write `content_revision` can still show the published-versus-current diff. A revision diff returns 404 when that revision was never stored.
