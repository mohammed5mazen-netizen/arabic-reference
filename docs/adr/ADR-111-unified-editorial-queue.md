# ADR-111: Unified editorial queue

Status: Accepted

Date: 2026-10-09

## Context

Each module already has a status, but no single list answered what is waiting, who created it, and where to open it.

## Decision

`editorial_record` unions the editorial content tables. The queue, review inbox, and publishing inbox are filtered queries on that view. Filters and sort columns are whitelisted. Each item carries type, title, status, actors, quality blockers, and a deep link. Sources, tool metrics, assistant data, and quiz attempts are excluded.

## Consequences

A column added to one content table does not appear in the queue until the view is updated in a new migration.
