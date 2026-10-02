# ADR-002 PostgreSQL as Primary Database

## Status

Accepted

## Date

2026-10-02

## Context

The linguistic model needs Unicode, relational integrity, revisions, and later full-text or trigram search. A separate search engine is not required to start.

## Decision

PostgreSQL is the system of record. Integration tests use PostgreSQL via Testcontainers, not H2. Schema changes use Flyway, and Hibernate validates rather than creates the schema.

## Consequences

Local development depends on Docker or another PostgreSQL instance. Search can start in PostgreSQL and move behind `LinguisticSearchPort` later.
