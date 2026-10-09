# ADR-127: Public slug history and redirects

Status: Deferred

Date: 2026-10-09

## Context

A published slug that changes would drop the old URL out of the index unless the old path redirects.

## Decision

Slug history is not implemented. Slugs are assigned once at creation by `ContentSlugs.of`, which keeps an id suffix, and update methods do not rewrite them. The sitemap therefore lists only the current path. Non-canonical encodings of that same slug redirect with 308 on word, root, and learning pages. Draft slugs do not redirect. Archived URLs return 404, not 410.

## Consequences

If a later change rewrites a published slug, this decision must be replaced with a history table and a 308 from the old published slug only. No migration was added for history. `V16` only grants `seo.admin.view`.
