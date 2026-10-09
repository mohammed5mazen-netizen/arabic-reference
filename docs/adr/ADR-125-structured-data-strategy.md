# ADR-125: Structured data strategy

Status: Accepted

Date: 2026-10-09

## Context

A page should advertise only a schema that matches its published facts.

## Decision

Homepage: `WebSite`. Breadcrumbs: `BreadcrumbList` from the visible crumbs. Articles: `Article` with real dates and an author only when the editor name is published. Words, roots, and grammar concepts: `DefinedTerm`. Learning paths are not `Course`. Organization is deferred until a real owner record exists. JSON-LD is escaped.

## Consequences

There is no generated image service and no invented legal entity. See `docs/STRUCTURED_DATA.md`.
