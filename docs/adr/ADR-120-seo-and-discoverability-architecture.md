# ADR-120: SEO and discoverability architecture

Status: Accepted

Date: 2026-10-09

## Context

Published pages were individually described, and the sitemap listed only the homepage. A crawler could not see the published catalog, and each page built its own canonical.

## Decision

One metadata builder, one origin policy, and one discovery read model. The discovery API reads published search documents. It does not call the assistant or Redis. Knowledge modules do not depend on the `seo` package.

## Consequences

A published row appears in search and in the sitemap together, and disappears from both when it is archived. SEO does not create pages that have no published record.
