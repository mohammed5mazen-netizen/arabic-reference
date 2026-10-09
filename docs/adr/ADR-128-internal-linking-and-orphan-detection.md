# ADR-128: Internal linking and orphan detection

Status: Accepted

Date: 2026-10-09

## Context

A sitemap URL with no inbound link is hard to discover. The homepage cannot link every word.

## Decision

Public pages link the relations they already store: a word to its root, related entries, and morphology; a grammar rule to its topic, concepts, related rules, and examples; articles to their published relations. Published learning references are listed on the word, grammar rule, and article, and the lesson already links to its knowledge references. The homepage is not required to link every entry.

An orphan class is a published dictionary entry with no root and no relation. It is an `INFO` finding, `DISCOVERABILITY_ORPHAN`, and a count on the SEO status page. Broken published relations are counted the same way. `INVALID_CANONICAL_SLUG` is a warning when a published slug is blank or contains whitespace, `%`, `?`, `#`, or `/`.

## Consequences

Orphan detection does not fail publication. A broken relation still uses the existing blocker.
