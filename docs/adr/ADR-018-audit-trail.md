# ADR-018 Audit Trail

## Status

Accepted

## Date

2026-10-03

## Context

Editorial actions have to be explainable later: who changed an account or a role, and whether a login failed. The log must not become a second copy of passwords or tokens.

## Decision

`admin_audit_event` records the actor when one exists, the event type, the target, the trace id, and a metadata object. Login failures may have a null actor. Metadata keys containing password, token, or secret are dropped before insert.

The table has no update or delete API. A database trigger rejects both. Reading it requires `admin.audit.view`.

## Consequences

Audit rows survive a rolled-back business action only when they were committed in their own transaction. Login failures are committed before the request returns `401`. Successful changes commit with the change.
