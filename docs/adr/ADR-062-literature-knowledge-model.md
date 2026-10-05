# ADR-062: Literature knowledge model

Status: Accepted

Date: 2026-10-05

## Context

The reference needs eras, people, works, genres, and schools. It must not become a store for complete books.

## Decision

`LiteraryFigure` covers poets and writers together. Aliases and roles are rows. Works store metadata and rights, not files. Public pages list only related records that are themselves published.

## Consequences

There is no "read the book" action. Full texts are out of scope. See ADR-064.
