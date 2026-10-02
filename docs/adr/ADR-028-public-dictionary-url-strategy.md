# ADR-028 Public Dictionary URL Strategy

## Status

Accepted

## Date

2026-10-03

## Context

Arabic slugs are allowed, but a lemma alone can belong to more than one published entry.

## Decision

The stable page is `/word/{slug}`. The slug is the normalized lemma, with spaces turned into hyphens, plus the first eight hex characters of the entry id. `/root/{slug}` uses the same shape for roots. The root API also accepts the normalized letters, so `/root/كتب` resolves when that root is published. Lookup results link to the slug, not to the bare lemma. Pages are server-rendered. The title is `{lemma} - المعنى والجذر`. The description is a published short definition or definition. If none exists, no meaning is invented.

## Consequences

Two entries spelled كتاب receive two URLs. The normalized form is not a second canonical URL.
