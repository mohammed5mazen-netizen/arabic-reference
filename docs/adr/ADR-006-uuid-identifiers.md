# ADR-006 UUID Identifier Strategy

## Status

Accepted

## Date

2026-10-02

## Context

Sequential public ids leak volume and are awkward when records are created in different editorial flows.

## Decision

Public identifiers for core entities are random UUIDs (`Ids.random()`). Database sequences may exist internally but are not the public resource id.

## Consequences

URLs and APIs should use UUIDs or human slugs, not raw serial numbers. S0 has no public linguistic entities yet.
