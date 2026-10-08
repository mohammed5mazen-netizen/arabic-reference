# ADR-097: Learning search integration

Status: Accepted

Date: 2026-10-09

## Context

A published lesson should be findable, and it should not bury an exact dictionary entry under lesson prose.

## Decision

Search types `LEARNING_PATH` and `LESSON` are indexed from the published snapshot's title and summary. The index version is 3. Archive removes the documents. Type priority places both after dictionary, root, grammar, spelling, rhetoric, literature, and articles. The public type filter `learning` returns only those two.

## Consequences

Rebuild includes published learning snapshots through `PublishedSearchSource`. Suggestions stay on the existing linguistic kinds and do not list lessons.
