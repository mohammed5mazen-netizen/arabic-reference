# ADR-011 Database Migration Strategy

## Status

Accepted

## Date

2026-10-02

## Context

Letting the ORM create tables hides schema history and drifts between environments.

## Decision

Flyway owns schema changes. Names follow `V1__description.sql`. Hibernate `ddl-auto=validate` checks mappings once entities exist. S0's only migration is a non-domain marker table so the pipeline is real.

## Consequences

Every schema change is a reviewed migration. Developers do not rely on `ddl-auto=update`.
