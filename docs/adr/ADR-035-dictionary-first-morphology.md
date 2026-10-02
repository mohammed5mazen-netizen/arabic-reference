# ADR-035 Dictionary-First Morphology

## Status

Accepted

## Date

2026-10-03

## Context

A rule can guess a root that the dictionary does not attest. That guess would look like a lexical fact.

## Decision

Analysis normalizes the input, looks up published S2 entries, then applies rules. A published manual reading outranks a dictionary hit, which outranks a rule. If no published root supports a rule, the candidate is omitted and `root` stays null.

## Consequences

Unpublished drafts do not appear in the public analyzer. Broken plurals are read from published lexical forms and are not generated.
