# ADR-072: Root explorer strategy

Status: Accepted

Date: 2026-10-05

## Context

A word can match a published root, a published entry's root, a morphology rule, or nothing.

## Decision

Try those sources in that order. A rule candidate is headed جذر محتمل. If none match, the tool says there is no documented root and does not invent one.

## Consequences

Disabling the supporting morphology rule removes the possible root on the next request, because the cache key includes the rule generation.
