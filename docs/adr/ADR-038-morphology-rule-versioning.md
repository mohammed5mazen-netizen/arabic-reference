# ADR-038 Morphology Rule Versioning

## Status

Accepted

## Date

2026-10-03

## Context

A later rule change will alter public analyses. Without a version, nobody can tell which engine produced a stored result.

## Decision

The code-backed set is identified by `RuleSet.VERSION`, currently `s3-sound-2026-10-03`. Public analyses and conjugation responses include it. Seeded rules store the same version. A morphology generation counter increments when a pattern, manual reading, or rule flag changes. Cache keys include the version, the generation, and a dictionary update stamp.

## Consequences

Changing a rule's behavior requires a new version constant and tests. Enabling a flag invalidates cached analyses through the generation counter.
