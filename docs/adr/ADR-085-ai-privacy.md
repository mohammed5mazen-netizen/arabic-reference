# ADR-085: AI privacy

Status: Accepted

Date: 2026-10-06

## Context

A public assistant can quietly become a transcript store. That is not required to operate it or to see whether it is failing.

## Decision

Do not store raw questions or answers. Usage rows and metrics are aggregates keyed by status, not by question text. Logs use a request id. Conversations are not persisted. Optional follow-up context is the last two turns supplied by the client for that request only.

## Consequences

Debugging a single answer needs a deliberate development setup. Production aggregates cannot reconstruct a visitor's question.
