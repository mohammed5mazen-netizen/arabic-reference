# ADR-010 API Versioning

## Status

Accepted

## Date

2026-10-02

## Context

Clients and the public site will live longer than individual endpoints.

## Decision

HTTP APIs live under `/api/v1`. Public reads use `/api/v1/public/...`. Editorial operations use `/api/v1/admin/...`. Errors use a stable JSON object: `code`, `message`, `details`, `fieldErrors`, `traceId`, `timestamp`. Success responses use `data`, `traceId`, and `timestamp`.

Breaking changes require a new version prefix. The public prefix cannot gain content-editing methods.

## Consequences

S0's foundation endpoint is the pattern later resources follow. Internal exceptions, SQL, and stack traces are not part of the contract.
