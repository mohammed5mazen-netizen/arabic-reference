# ADR-050: Unified linguistic search

Status: Accepted

Date: 2026-10-05

## Context

Readers should not have to know whether a word lives in the dictionary, the roots, or the grammar. S0 left `LinguisticSearchPort` empty so a later stage could fill it without rewriting callers.

## Decision

One public query, `GET /api/v1/public/search`, and one suggestion query. The search module owns ranking and the result card. Dictionary lookup and grammar search stay as specialized endpoints. The morphology tool stays a separate page. Controllers do not merge repositories.

## Consequences

The homepage and `/search` both use this API. Adding a knowledge type later means publishing a `SearchDocument`, not a new results page.
