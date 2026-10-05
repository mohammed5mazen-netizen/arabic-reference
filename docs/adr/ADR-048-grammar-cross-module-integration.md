# ADR-048: Grammar cross-module integration

Status: Accepted

Date: 2026-10-03

## Context

An annotated token may point at a dictionary entry and a morphology analysis. Those links must not create a cycle or let grammar domain code see the other modules' tables.

## Decision

`GrammarCrossLinks` is a port in `grammar.domain`. `GrammarCrossLinkAdapter` in `grammar.application` calls `PublishedDictionaryQuery` and `PublishedMorphologyQuery`. A link is copied into the annotation snapshot only when the target is published. Morphology does not depend on grammar.

## Consequences

Architecture tests reject `grammar.domain` depending on dictionary or morphology, and they reject morphology depending on grammar. The public token can link to `/word/{slug}` and can show the published pattern.
