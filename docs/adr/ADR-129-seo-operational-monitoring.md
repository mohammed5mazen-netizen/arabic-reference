# ADR-129: SEO operational monitoring

Status: Accepted

Date: 2026-10-09

## Context

Operators need to see whether indexing is on, which origin will be advertised, and whether the sitemap and the public graph disagree.

## Decision

`GET /api/v1/admin/seo/status` returns the indexing flag, the parsed origin, counts by type, the last discovery read, orphan and broken-relation counts, invalid paths, and problem strings. `/admin/seo` renders that payload. There is no analytics collector and no Search Console client. A verification meta tag is rendered only from `GOOGLE_SITE_VERIFICATION` when set.

## Consequences

The status view is permission `seo.admin.view`. It does not include secrets. It is not a metadata editor.
