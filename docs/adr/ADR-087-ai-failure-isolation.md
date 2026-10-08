# ADR-087: AI failure isolation

Status: Accepted

Date: 2026-10-06

## Context

A provider timeout must not break search, tools, or publishing.

## Decision

Provider failures become `503` with one Arabic sentence and a `PROVIDER_ERROR` usage row. The response does not include provider status text. After three consecutive infrastructure failures the adapter opens a short circuit and stops calling out. The assistant can also be turned off with `AI_ENABLED=false`. No Resilience4j dependency is added.

## Consequences

S0–S7 keep serving when the model host is down or the assistant is disabled.
