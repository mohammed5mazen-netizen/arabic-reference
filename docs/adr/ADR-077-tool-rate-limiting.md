# ADR-077: Tool rate limiting

Status: Accepted

Date: 2026-10-05

## Context

The tools are anonymous and some of them fan out across modules.

## Decision

A shared per-minute Redis counter applies to tool execution. The default is 90 requests. The response is HTTP 429 with an Arabic message. Redis failure does not block the visitor. Login is not required.

## Consequences

Tests can lower the ceiling and observe 429. Ordinary reading stays open.
