# ADR-090: Learning platform architecture

Status: Accepted

Date: 2026-10-09

## Context

The reference already publishes dictionary, grammar, morphology, spelling, rhetoric, literature, articles, search, tools, and a grounded assistant. Teaching needs a sequence on top of that knowledge without a second copy of it and without a login wall.

## Decision

Add a `learning` module with `api`, `application`, `domain`, and `infrastructure`. The public site stays readable without an account. Learner accounts are deferred. The root package stays a marker. Domain code does not use Spring, JPA, or another module. Knowledge modules, tools, and `ai` do not depend on `learning`.

## Consequences

Anonymous visitors can read published paths and lessons and submit quizzes. Staff edit through the admin API and the existing editorial lifecycle.
