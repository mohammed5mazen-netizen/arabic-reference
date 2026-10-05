# ADR-046: Grammar editorial lifecycle

Status: Accepted

Date: 2026-10-03

## Context

Topics, rules, concepts, and annotations need the same review path as dictionary and morphology entries.

## Decision

They use `EditorialWorkflow`: draft, in review, changes requested, verified, published, archived. The creator cannot review. The publisher cannot be the creator or the reviewer. `grammar.rule.review`, `grammar.rule.publish`, `grammar.rule.archive`, and `grammar.rule.submit` apply to all four aggregates so the permission list stays the one granted to the existing roles. Optimistic version conflicts return 409.

## Consequences

Editors create and submit. Reviewers review. Publishers publish and archive. Administrators can view. The platform owner receives the permissions through the existing `grammar.%` grant, not through a bypass in code.
