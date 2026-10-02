# ADR-022 Sense-Level Semantics

## Status

Accepted

## Date

2026-10-03

## Context

Synonymy and domain labels attach to a meaning, not to every use of a written word.

## Decision

`LexicalSense` is independent and ordered by `displayOrder`. Usage labels and semantic domains are small controlled vocabularies, not free text and not a full ontology. Synonym, antonym, and related links join two senses. Multiple sources attach to the same sense through citations.

## Consequences

Publishing a sense does not copy it once per dictionary. A new sense is a new row with its own order.
