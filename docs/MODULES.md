# Modules

The backend is one process with explicit package boundaries. S10 does not add a module: navigation, tokens, and shared states live in the frontend. A module may depend on `shared` and on its own domain. It must not reach into another module's infrastructure, and domain packages must not depend on API or infrastructure packages.

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

`LinguisticSearchPort` is the read port. `SearchIndex` is the write port. PostgreSQL is the first adapter (`PostgresLinguisticSearchAdapter`). No Elasticsearch or OpenSearch client is on the classpath. Dictionary and grammar publish into the index through those ports. Search does not read their repositories. See `docs/SEARCH_ARCHITECTURE.md`.

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

## grammar

Topics, rules, typed components, concepts, aliases, examples, and manual sentence annotations. Public reads use published snapshots. The domain calls dictionary and morphology only through `GrammarCrossLinks`. It does not depend on their infrastructure, and morphology does not depend on grammar. See [GRAMMAR_KNOWLEDGE_MODEL.md](GRAMMAR_KNOWLEDGE_MODEL.md).

## spelling

Topics, rules, clauses, and examples for Arabic orthography. Public pages read published snapshots. See [SPELLING_KNOWLEDGE_MODEL.md](SPELLING_KNOWLEDGE_MODEL.md).

## rhetoric

Topics, devices, components, examples, interpretations, and relations. See [RHETORIC_KNOWLEDGE_MODEL.md](RHETORIC_KNOWLEDGE_MODEL.md).

## literature

Eras, figures, aliases, roles, works, genres, schools, and rights. Dates use `HistoricalDate`. Excerpts follow [LITERATURE_RIGHTS_POLICY.md](LITERATURE_RIGHTS_POLICY.md). See [LITERATURE_KNOWLEDGE_MODEL.md](LITERATURE_KNOWLEDGE_MODEL.md).

## content

Articles, sections, tags, and cross-domain relations. The root `content` package stays a package marker; article code lives in the subpackages. See [ARTICLE_CONTENT_MODEL.md](ARTICLE_CONTENT_MODEL.md).

## tools

Orchestration for the public linguistic tools. The root `tools` package stays a package marker. Application services call published query ports and `LinguisticSearchPort`. They do not call foreign repositories or controllers. See [LINGUISTIC_TOOLS.md](LINGUISTIC_TOOLS.md).

## ai

Retrieval-first linguistic assistant. `ai.domain` holds intents, evidence, and the prompt version. Application ports are `AiModelPort`, `KnowledgeRetrievalPort`, `AiUsagePort`, `AiRateLimitPort`, and `AiAnswerCachePort`. Infrastructure implements them. There is no embedding adapter. The root `ai` package stays a package marker. See [RAG_ARCHITECTURE.md](RAG_ARCHITECTURE.md).

## learning

Paths, units, lessons, activities, and quizzes. The root `learning` package stays a package marker. Domain code does not use Spring or JPA. References to dictionary, grammar, spelling, rhetoric, articles, and the morphology tool go through `PublishedReferencePort`. Learning does not depend on `ai` or on another module's infrastructure. See [LEARNING_PLATFORM.md](LEARNING_PLATFORM.md).
