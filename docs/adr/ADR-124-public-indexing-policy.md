# ADR-124: Public indexing policy

Status: Accepted

Date: 2026-10-09

## Context

Search results, tool queries, and assistant questions are states of a tool, not documents. Admin and login pages are private.

## Decision

Hubs and published knowledge pages are indexable when the environment flag is on. `/search`, tool query states, and `/assistant?q=` are `noindex`. Their canonicals omit the query. Quiz attempts are not pages. Noindex is not treated as access control.

## Consequences

Empty hubs stay 200. Missing detail pages stay 404. See `docs/INDEXING_POLICY.md`.
