# ADR-122: Dynamic sitemap strategy

Status: Accepted

Date: 2026-10-09

## Context

The homepage-only sitemap could not list dictionary, grammar, content, or learning URLs, and loading every row into memory would not scale.

## Decision

A sitemap index splits core hubs from knowledge chunks of 5,000 URLs. Knowledge URLs come from a paged query of `search_document`, at most 500 rows at a time. `lastModified` is `published_at`. `changefreq` and `priority` are omitted.

## Consequences

The index can grow to ten chunks before a further split is required. Unpublished and private paths are filtered in SQL and again in the domain check.
