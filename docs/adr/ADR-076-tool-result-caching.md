# ADR-076: Tool result caching

Status: Accepted

Date: 2026-10-05

## Context

The tools are deterministic for a given published generation, so repeating a query should not recompute it. A separate cache per tool would drift.

## Decision

One Redis cache stores every tool envelope. The key is the tool, the normalized input, the dictionary content stamp, the morphology rule generation, and the search index version. Tool results are not written to PostgreSQL. A cache failure leaves the request uncached and still answers it.

## Consequences

Publish, archive, reindex, and rule changes select a new key. The old entry expires with the shared TTL.
