# ADR-121: Production site URL policy

Status: Accepted

Date: 2026-10-09

## Context

`resolveSiteUrl()` used to return `http://localhost:3000` whenever `SITE_URL` was missing. A production process would then advertise that origin.

## Decision

`SITE_URL` is the source of truth. `NEXT_PUBLIC_SITE_URL` applies only if `SITE_URL` is empty. The value must be an absolute http(s) origin with no query, fragment, or path. Outside production, and during `next build`, a missing value may still be `http://localhost:3000`. `next start` throws if it is missing. Indexing additionally requires `https` and a non-local host.

## Consequences

Local `next start` and the public-access test set `SITE_URL` explicitly. A forgotten production variable fails the page instead of emitting a localhost canonical.
