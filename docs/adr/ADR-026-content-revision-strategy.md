# ADR-026 Content Revision Strategy

## Status

Accepted

## Date

2026-10-03

## Context

Audit events record that something happened. They are not a recoverable copy of a published entry.

## Decision

`content_revision` stores `target_type`, `target_id`, a monotonic `revision_number`, a JSON snapshot, the actor, an optional reason, and the time. Opening a published entry, root, or source for edit writes a revision before the working copy returns to draft. Audit metadata stays limited to identifiers, codes, and status.

## Consequences

History can answer who changed a record, when, and which previous snapshot existed. It does not replace the audit trail, and the audit trail does not replace it.
