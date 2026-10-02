# ADR-023 Linguistic Relation Model

## Status

Accepted

## Date

2026-10-03

## Context

S2 needs synonyms, antonyms, related meanings, and known derivations without becoming a general knowledge graph or a morphology engine.

## Decision

`LinguisticRelation` allows only `SYNONYM`, `ANTONYM`, `RELATED`, and `DERIVED_FROM`. The first three are sense-to-sense and unique in either direction. `DERIVED_FROM` is directional between two different entries. Verification is `REPORTED` or `REVIEWED`. A public relation appears only when it is published and the other entry has a published snapshot.

## Consequences

Derivations such as كتب → كاتب are stored when an editor records them. S3 may generate candidates later. S2 does not.
