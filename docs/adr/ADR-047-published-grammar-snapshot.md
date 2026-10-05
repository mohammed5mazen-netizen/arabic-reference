# ADR-047: Published grammar snapshot

Status: Accepted

Date: 2026-10-03

## Context

Editing a published rule must not change the public page until the new revision is published.

## Decision

Each aggregate stores a JSON snapshot and published columns (`published_title`, `published_normalized`, `published_parent_id`, `published_topic_id`, and the concept summary). Public reads use those fields. Children and rules of a topic are other published records, so a newly published child appears without republishing the parent. Relations and prerequisites inside a snapshot stay as they were at publication, and a target that is no longer published is omitted from later public composition where the read walks current publication.

## Consequences

Draft titles do not appear in search. Archive removes the snapshot and the published columns.
