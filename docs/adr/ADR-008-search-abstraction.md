# ADR-008 Search Abstraction

## Status

Accepted

## Date

2026-10-02

## Context

Arabic search may begin with PostgreSQL and later need another engine for fuzzy or morphological search. Choosing that engine now would freeze an unused dependency.

## Decision

`LinguisticSearchPort` in the search domain is the extension point. S0 registers no adapter and does not depend on Elasticsearch or OpenSearch. The port is read-only and is not a source of linguistic truth.

## Consequences

S5 can add an adapter without rewriting dictionary or grammar packages. The port's query model stays undefined until there is a real search use case.
