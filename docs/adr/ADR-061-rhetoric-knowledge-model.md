# ADR-061: Rhetoric knowledge model

Status: Accepted

Date: 2026-10-05

## Context

Rhetorical devices need definitions, components, examples, and explicit relations. A single interpretation must not be stored as the only truth.

## Decision

The `rhetoric` module stores topics, devices, components, examples, and relations. Examples may carry an interpretation, a scholarly note, and an alternative interpretation. Quoted examples require a citation. A device cannot relate to itself.

## Consequences

The public device page shows more than one reading when the editor recorded one. Categories can grow beyond المعاني، البيان، and البديع.
