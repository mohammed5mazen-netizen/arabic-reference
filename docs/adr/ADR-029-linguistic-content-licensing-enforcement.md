# ADR-029 Linguistic Content Licensing Enforcement

## Status

Accepted

## Date

2026-10-03

## Context

Unknown provenance is not permission to publish. Restricted material must not reach the public site by accident.

## Decision

Licenses are `PUBLIC_DOMAIN`, `CC0`, `CC_BY`, `CC_BY_SA`, `PERMISSION_GRANTED`, `RESTRICTED`, and `UNKNOWN`. Only the first five may be published. `publicDomain` may be set only for `PUBLIC_DOMAIN` and `CC0`. A source with `RESTRICTED` or `UNKNOWN` can be catalogued and cannot be published. A published sense cannot cite an unpublished source or a source whose license is not publicly attributable. The public API returns attribution fields and omits license-administration notes.

No production migration seeds dictionary text. Tests create their own fixtures. A development demonstration, if created through the API, is labeled as such and is not a Flyway seed.

## Consequences

Import of unsourced text has no path that skips license metadata. `UNKNOWN` remains a block, not a default allow.
