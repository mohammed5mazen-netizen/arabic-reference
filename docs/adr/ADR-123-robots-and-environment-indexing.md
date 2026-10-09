# ADR-123: Robots and environment indexing

Status: Accepted

Date: 2026-10-09

## Context

Development and future preview hosts must not be indexed, and production indexing must be an explicit choice.

## Decision

`SEO_INDEXING_ENABLED` defaults to false. False disallows every path in `robots.txt` and sets `noindex` on pages. True allows public content and disallows `/admin/`, `/api/`, and `/search`. Both modes point `Sitemap` at the configured origin.

## Consequences

A preview deployment is safe if it does not set the flag. Turning indexing on with a local or `http` origin fails in the frontend.
