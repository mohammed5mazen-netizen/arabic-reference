# ADR-067: S6 search integration

Status: Accepted

Date: 2026-10-05

## Context

Published spelling, rhetoric, literature, and articles must appear in the S5 index without weakening an exact dictionary hit.

## Decision

Publish and archive update `SearchIndex` inside the same transaction, after the search advisory lock. New types sit after the existing ones in the tie-break: spelling rule, rhetoric device, literary figure, literary work, article, then topics and eras. Suggestions may use spelling-rule, rhetoric-device, and figure titles, including aliases. Article bodies are not suggested. `indexVersion` is 2.

## Consequences

Rebuild and repair already walk every `PublishedSearchSource`, so S6 snapshots are included. An exact dictionary title still outranks an article body match.
