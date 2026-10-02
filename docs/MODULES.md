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

Arabic text handling, editorial lifecycle types, content slugs, and content revisions. Dictionary and source both use this package so they do not depend on each other for those shared types. Morphology still does not live here.

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

## dictionary

Lexical entries, senses, forms, usage examples, roots, and linguistic relations. Public reads and admin commands go through application services. The module may call the source application to read citations. It does not call source repositories, and it does not depend on morphology or AI.

## source

Reference works, licenses, and citations. It does not depend on the dictionary module. Dictionary evidence tables hold the foreign keys back to citations.

## morphology

Patterns, manual morphological readings, clitic segmentation, and a dictionary-first analyzer. It reads published dictionary data through `PublishedDictionaryQuery` and does not let dictionary code depend on it. Conjugation covers a sound triliteral فَعَلَ only when the class and, for the imperfect, the stem vowel are recorded. See [MORPHOLOGY_ENGINE.md](MORPHOLOGY_ENGINE.md).

## grammar, content, learning, ai

Package placeholders. Architecture tests fail if these packages gain implementation classes. Sentence syntax waits for a later stage.
