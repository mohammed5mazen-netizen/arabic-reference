# ADR-059: Search privacy and metrics

Status: Accepted

Date: 2026-10-05

## Context

Public search is anonymous. Storing every query against a person would create a history the product does not have.

## Decision

No per-user search log. Audit records only rebuild and repair. Metrics are aggregate counters and a timer, without the raw query as a label or a log field. The page cache is process-local and keyed by generation, index version, display query, normalized key, filters, page, and size. A high anonymous rate limit applies. Login is not required.

## Consequences

Query text still appears in the result page because the reader typed it. It is rendered as text. It is not retained as a profile.
