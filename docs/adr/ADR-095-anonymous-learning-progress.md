# ADR-095: Anonymous learning progress

Status: Accepted

Date: 2026-10-09

## Context

Visitors should resume a path without creating an account, and the server should not collect a profile of anonymous reading.

## Decision

Progress lives in `localStorage` schema version 1: completed lesson slugs, quiz scores, and the last lesson. Completion is an explicit action or a submitted quiz, not a page view. The server does not store anonymous progress. Learner accounts and email verification are deferred. `AdminUser` is not reused for learners.

## Consequences

Clearing site data clears progress. A later account can sync a new server model without a migration of anonymous rows, because none exist.
