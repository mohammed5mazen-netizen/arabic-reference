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

`/api/v1/admin/**` is the editorial boundary for a future platform owner, admin, editor, linguistic reviewer, and content reviewer. S0 exposes the route and rejects anonymous calls. It does not implement staff identity or content management.

## identity

Empty boundary. S1 will add **admin** identity and RBAC here. It will not become a public login wall.

## dictionary, grammar, morphology, content, source, learning, ai

Package placeholders. Their future ownership:

- dictionary: lemmas, meanings, roots, synonyms, antonyms, number, gender
- grammar: syntactic rules and i'rab
- morphology: derivation and conjugation
- content: articles and publication lifecycle
- source: works, editions, licenses, citations
- learning: lessons and optional learner state
- ai: assistant and retrieval that cites `source` and never replaces it

Architecture tests fail if these packages gain implementation classes during S0.
