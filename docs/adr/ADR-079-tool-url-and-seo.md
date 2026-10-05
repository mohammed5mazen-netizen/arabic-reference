# ADR-079: Tool URL and SEO strategy

Status: Accepted

Date: 2026-10-05

## Context

Shareable queries are useful, and indexing every query would create an unbounded set of thin pages.

## Decision

`/tools` and each tool page without a query are indexable. A URL that carries `q`, `word`, `a`, or `b` canonicalizes to the tool base and is `noindex`. The origin is `SITE_URL`, then `NEXT_PUBLIC_SITE_URL`. Sitemap expansion remains S12.

## Consequences

A shared link still renders the query. Search engines are pointed at the tool page.
