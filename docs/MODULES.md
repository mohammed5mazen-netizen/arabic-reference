# Modules

The backend is one process with explicit package boundaries. A module may depend on `shared` and on its own domain. It must not reach into another module's infrastructure, and domain packages must not depend on API or infrastructure packages.

## shared

Cross-cutting kernel and delivery:

- stable API error model
- UTC `TimeProvider`
- UUID helper
- trace id filter
- stateless security policy
- `GET /api/v1/public/foundation`

## linguistics

Arabic text handling. S0 owns normalization only: original text stays intact and a separate normalized form is derived. Morphology and dictionary logic do not live here yet.

## search

`LinguisticSearchPort` is the future read port. No Elasticsearch or OpenSearch client is on the classpath. The first adapter can use PostgreSQL. A later adapter can replace it.

## admin

`/api/v1/admin/**` is the editorial HTTP boundary. Controllers call identity services. They do not talk to repositories. Anonymous calls still fail closed, except login and refresh.

## identity

Editorial accounts only. S1 owns:

- `AdminUser` with UUID, normalized username and email, Argon2id password hash, status, and optimistic version
- system roles `PLATFORM_OWNER`, `ADMIN`, `EDITOR`, `LINGUISTIC_REVIEWER`, `PUBLISHER`, `AUDITOR`
- granular permission codes, including future editorial permissions that do not yet have content tables
- short-lived access tokens and rotating refresh tokens
- append-only audit events
- one-time platform-owner bootstrap

It is not a visitor account system.

## dictionary, grammar, morphology, content, source, learning, ai

Package placeholders. Their future ownership:

- dictionary: lemmas, meanings, roots, synonyms, antonyms, number, gender
- grammar: syntactic rules and i'rab
- morphology: derivation and conjugation
- content: articles and publication lifecycle
- source: works, editions, licenses, citations
- learning: lessons and optional learner state
- ai: assistant and retrieval that cites `source` and never replaces it

Architecture tests fail if these packages gain implementation classes during S1. Editorial permissions for them exist so later stages can attach behavior without inventing a second authorization model.
