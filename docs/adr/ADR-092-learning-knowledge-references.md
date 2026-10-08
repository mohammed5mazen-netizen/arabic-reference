# ADR-092: Learning knowledge references

Status: Accepted

Date: 2026-10-09

## Context

A lesson should send the reader to the published rule or entry. A free-form `entity_type` plus an unchecked id can point at a draft or a missing row.

## Decision

References use `ReferenceKind` and the public slug. `PublishedReferencePort` resolves only a published record. The public page resolves the title and URL again, so the link tracks the latest published knowledge. The lesson's own wording stays in the path snapshot. An optional note on the reference is snapshotted with the lesson when a stable explanation is needed.

## Consequences

An unpublished slug cannot be saved. A reference that is later archived renders as no longer published instead of a broken internal id.
