# ADR-109: Canonical URL and slug handling

Status: Accepted

Date: 2026-10-09

## Context

Arabic slugs arrive percent-encoded. Encoding them again produces a `%25` path and a 404. Canonical URLs must follow the request environment, not a build-time localhost.

## Decision

`learningSlug` and `canonicalSlug` decode at most twice, then the API call encodes once. Canonical URLs use `resolveSiteUrl()`, which reads `SITE_URL`, then `NEXT_PUBLIC_SITE_URL`, and only then `http://localhost:3000`, at request time where the page calls `connection()`. Search, tool queries, assistant questions, and quiz results stay `noindex` or are not URLs. `publicMetadata` is the shared builder for title, description, canonical, and Open Graph.

## Consequences

A regression test covers a raw slug, an encoded slug, and a canonical without `%25`. Pages that still build metadata inline should move to `publicMetadata` when they are next edited.
