# ADR-030 Morphology Engine Architecture

## Status

Accepted

## Date

2026-10-03

## Context

S2 stores lemmas, roots, forms, and citations. S3 needs to analyze a surface form without turning the dictionary module into a morphology engine, and without claiming full Arabic coverage.

## Decision

Morphology is its own module. Domain code calls a `PublishedLexicon` port. The adapter uses `dictionary.application.PublishedDictionaryQuery`. Dictionary and source packages do not depend on morphology. The public analyzer, conjugation planner, and pattern catalog live behind that boundary. Coverage is the subset documented in `docs/MORPHOLOGY_ENGINE.md`.

## Consequences

A later grammar module can read morphology through an application port. It must not reach into morphology repositories. S3 does not add a sentence parser.
