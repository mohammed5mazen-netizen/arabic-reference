# ADR-039 Public Morphology API

## Status

Accepted

## Date

2026-10-03

## Context

The morphology tool is part of the open reference. ADR-013 forbids a login wall in front of ordinary reading.

## Decision

These routes stay under `/api/v1/public` and allow anonymous GET:

- `/api/v1/public/morphology/analyze`
- `/api/v1/public/morphology/conjugate`
- `/api/v1/public/morphology/roots/{slug}`
- `/api/v1/public/dictionary/entries/{id}/morphology`

Admin morphology stays under `/api/v1/admin/morphology` and requires a staff token plus a permission. A generous per-minute limiter protects analysis and fails open if Redis is unavailable. It does not require an account.

## Consequences

The public site still has no visitor login. Missing admin permission remains HTTP 403, and a missing token remains HTTP 401.
