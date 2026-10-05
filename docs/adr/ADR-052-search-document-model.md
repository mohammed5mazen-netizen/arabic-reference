# ADR-052: Search document model

Status: Accepted

Date: 2026-10-05

## Context

Search needs one row shape for entries, roots, topics, rules, and concepts, without returning the editorial entity.

## Decision

`SearchDocument` stores the display title, the S0 normalized title, the search key, searchable text, optional root, alias and form tokens, a short snippet, a URL, and `indexVersion`. Popularity and source quality are stored as zeros for a later ranking signal. Citations and internal notes are not stored. Morphology patterns are not documents.

## Consequences

The public result is a card: type, id, title, subtitle, snippet, url, match reason, highlight ranges, and a small metadata map. The numeric score is not on the card.
