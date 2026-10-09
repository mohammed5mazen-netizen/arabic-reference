# ADR-117: Persistent quality findings

Status: Accepted

Date: 2026-10-09

## Context

The quality dashboard needs counts without loading every entity, and a rescan must not leave old blockers open.

## Decision

Each scan writes `quality_scan` and replaces findings for the scanned records: previous `OPEN` rows become `STALE`, then current findings are inserted as `OPEN`. The audit log records the scan once.

## Consequences

Findings can be briefly stale until the next scan. The scale path, if a full scan no longer fits a request, is a database job in this process, not a broker.
