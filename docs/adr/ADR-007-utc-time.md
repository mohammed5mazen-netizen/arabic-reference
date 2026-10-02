# ADR-007 UTC Time Strategy

## Status

Accepted

## Date

2026-10-02

## Context

Editorial history and publication times must not depend on the server's local zone.

## Decision

Important timestamps are `Instant` values from `TimeProvider`. The database uses `timestamptz`. Hibernate's JDBC time zone is UTC.

## Consequences

Display time zones, if needed, are a presentation concern. Domain code does not call `LocalDateTime.now()`.
