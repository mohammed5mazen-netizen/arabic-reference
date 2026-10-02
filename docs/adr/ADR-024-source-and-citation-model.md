# ADR-024 Source and Citation Model

## Status

Accepted

## Date

2026-10-03

## Context

A linguistic claim needs a recoverable place in a source, and one claim may be supported by several sources.

## Decision

`ReferenceSource` and `SourceCitation` live in the source module. Dictionary evidence uses separate foreign-key tables for senses, entries, relations, and roots, plus a citation column on quoted examples. Pages, volume, chapter, and locator are optional. A citation range must be positive and ordered.

Every published sense needs at least one citation. A root may be published without one because it is a structural record, not a definition. A quoted example requires a citation.

## Consequences

The public response can name the source, author, edition, and page when they exist. Copyright internals beyond attribution stay off the public payload.
