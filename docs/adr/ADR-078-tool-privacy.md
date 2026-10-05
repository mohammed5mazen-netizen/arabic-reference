# ADR-078: Tool privacy

Status: Accepted

Date: 2026-10-05

## Context

Tool input can be a word a visitor is unsure about. Storing it would build a search history the product does not want.

## Decision

S7 keeps no visitor profile, no tool history, and no fingerprint. Metrics use the tool code, the outcome, and the duration. Logs do not include the raw input.

## Consequences

`/admin/tools` shows aggregates only.
