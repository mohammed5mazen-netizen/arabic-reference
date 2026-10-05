# ADR-051: PostgreSQL-first search

Status: Accepted

Date: 2026-10-05

## Context

The published corpus is still small. A separate search cluster would add operations without a measured need.

## Decision

The first `LinguisticSearchPort` adapter is PostgreSQL: normalized columns, btree indexes, and `pg_trgm` GIN indexes for contains and similarity. The application depends on the port, not on SQL. An OpenSearch adapter can replace it later. S5 ships no search-engine client.

## Consequences

`V6__linguistic_search.sql` creates the extension. Hosts must allow `CREATE EXTENSION pg_trgm`. Query shapes are documented in `docs/SEARCH_ARCHITECTURE.md`.
