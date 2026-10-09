# ADR-114: Content quality framework

Status: Accepted

Date: 2026-10-09

## Context

Editors needed a repeatable way to see missing titles, citations, rights, and broken relations. A model opinion is not testable and must not block publication.

## Decision

`QualityRules` evaluates a probe filled by SQL. Severities are `INFO`, `WARNING`, and `BLOCKER`. Universal rules and a sample of dictionary, grammar, spelling, literature, article, and learning rules are pure functions. The assistant is not a reviewer, publisher, or licensing authority.

## Consequences

A new rule is a code change with a unit test. The dashboard does not invent workload numbers.
