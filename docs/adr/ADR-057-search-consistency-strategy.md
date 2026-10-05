# ADR-057: Search consistency strategy

Status: Accepted

Date: 2026-10-05

## Context

A document can be missing after a partial failure, or it can point at a record that is no longer public.

## Decision

Status compares published snapshot identities with stored documents and with `indexVersion`. Repair rebuilds when any are missing, stale, or outdated, and records `SEARCH_INDEX_REPAIR`. A consistent index is left unchanged and is not audited. Rebuild records `SEARCH_INDEX_REBUILT`.

## Consequences

The admin page shows the counts. It does not list private editorial fields.
