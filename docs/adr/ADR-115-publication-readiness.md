# ADR-115: Publication readiness

Status: Accepted

Date: 2026-10-09

## Context

A publisher needs a checklist before publish, but a dashboard must not become a second, weaker publish path.

## Decision

Readiness returns `READY` or `BLOCKED` with reasons and a checklist. Domain publish methods remain the authority. An open blocker finding is an additional refusal through `PublicationChecks`, after the domain's own checks.

## Consequences

Fixing the content without rescanning can leave an old blocker in place, so publish stays blocked until the finding is stale.
