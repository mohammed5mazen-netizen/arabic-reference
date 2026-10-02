# ADR-033 Root and Pattern Compatibility

## Status

Accepted

## Date

2026-10-03

## Context

A triliteral pattern such as فَعَلَ is not a quadriliteral pattern. Applying it silently produces a false analysis.

## Decision

Every pattern stores `radicalCount`. A candidate or a saved reading is rejected when the published root's radical count differs. A missing root does not cause the engine to invent letters. Quadriliteral patterns such as فَعْلَلَ are catalog data; S3 does not conjugate them.

## Consequences

Editors see a validation error. The public analyzer omits the incompatible candidate.
