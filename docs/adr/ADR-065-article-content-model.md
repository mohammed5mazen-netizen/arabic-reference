# ADR-065: Article content model

Status: Accepted

Date: 2026-10-05

## Context

Some explanations are longer than a rule or a dictionary sense, and they should not replace those structured models.

## Decision

The `content` module stores articles, ordered sections, normalized tags, and citations at article or section level. The root `content` package stays a package marker. Articles do not become the storage for spelling, rhetoric, or grammar rules.

## Consequences

Publication requires a section and a citation. Public readers see published articles only.
