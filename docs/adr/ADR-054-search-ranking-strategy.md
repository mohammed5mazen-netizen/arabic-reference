# ADR-054: Search ranking strategy

Status: Accepted

Date: 2026-10-05

## Context

Several match kinds can hit one query. The order has to be explainable and stable.

## Decision

An integer score per match reason, documented in `docs/SEARCH_RANKING.md`. Exact original title outranks a definition hit. A normalized exact title outranks fuzzy. A published form outranks fuzzy. A morphology candidate outranks a fuzzy body and stays below an exact dictionary title. Ties break by type priority, normalized title, then id. The score is not displayed.

## Consequences

Tests can assert order without depending on a search-engine score plugin. Changing the scale means a new `indexVersion` only when the stored document changes; the score itself is computed at query time.
