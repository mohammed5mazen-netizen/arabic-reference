# Architecture

Arabic Reference is a modular monolith. One deployable backend owns the linguistic domain, and a separate Next.js frontend renders the public reference. Microservices are intentionally out of scope.

## Public-first access

The visitor journey is:

`Domain → Home Page → Search / Browse`

It is not `Domain → Login → Application`.

Ordinary reading and search of public linguistic knowledge must never require an account. Authentication, when it arrives, is for internal editorial administration or for optional personal features. Local preferences such as theme stay in `localStorage`.

Permanent rule:

> Arabic Reference is an open linguistic reference. Authentication must never become a prerequisite for ordinary access to public linguistic knowledge.

## Request paths

Future public pages, not built in S0:

`/`, `/search`, `/dictionary`, `/word/[slug]`, `/root/[root]`, `/grammar`, `/morphology`, `/spelling`, `/rhetoric`, `/literature`, `/articles`, `/sources`, `/tools`, `/learn`

S0 implements `/` only. These paths are architectural examples so later routes stay crawlable and free of an authentication middleware.

API conventions:

- `GET /api/v1/public/**` — anonymous reads
- `POST`, `PUT`, `PATCH`, `DELETE` under `/api/**` — not public
- `/api/v1/admin/**` — editorial staff only. `POST /api/v1/admin/auth/login` and `/refresh` are the staff entry points. Other admin routes require a bearer token and a permission.

The public API must not grow into a content-editing API.

## Backend modules

Base package: `com.mrsoft.arabicreference`

| Module | S1 contents |
| --- | --- |
| `shared` | Errors, time, ids, trace id, security filter, public foundation endpoint |
| `linguistics` | `ArabicTextNormalizer` |
| `search` | Unified published search: `LinguisticSearchPort`, `SearchIndex`, PostgreSQL adapter |
| `admin` | Staff HTTP API. It calls identity services and does not own accounts. |
| `identity` | Editorial accounts, roles, permissions, tokens, audit, owner bootstrap |
| `dictionary` | Lexical entries, senses, forms, relations, roots, public lookup |
| `source` | Works, licenses, citations |
| `linguistics` | Normalization, editorial states, slugs, content revisions |
| `morphology` | Patterns, manual readings, a bounded rule analyzer, and limited sound-verb conjugation |
| `grammar` | Topics, rules, concepts, examples, and manual syntax annotations |
| `content` | Articles. The root package stays a marker |
| `learning` | Paths, lessons, and quizzes over published knowledge. The root package stays a marker |
| `ai` | Retrieval-first assistant over published knowledge |

Domain code does not depend on web or persistence. Controllers do not call repositories.

## Linguistic knowledge model

The core is a knowledge model, not a generic CMS. S2 implements:

`Surface form → Lexical entry / lemma → optional root → senses → forms → relations → examples → sources → citations`

S3 adds a bounded morphology engine beside that model. It is documented in [MORPHOLOGY_ENGINE.md](MORPHOLOGY_ENGINE.md). S4 adds structured grammar, documented in [GRAMMAR_KNOWLEDGE_MODEL.md](GRAMMAR_KNOWLEDGE_MODEL.md) and [SYNTAX_ANNOTATION_MODEL.md](SYNTAX_ANNOTATION_MODEL.md). The dictionary model is described in [DICTIONARY_MODEL.md](DICTIONARY_MODEL.md).

Every published sense points at a citation. Entries also carry contributor, reviewer, verification state, revision, and publication state. See ADR-004, ADR-020, and ADR-025.

Editorial lifecycle:

`DRAFT → IN_REVIEW → VERIFIED → PUBLISHED → ARCHIVED`

`CHANGES_REQUESTED` returns an item from review. A verified record that fails publication can also return to `CHANGES_REQUESTED`, so it becomes editable again. A published edit keeps the last public snapshot and opens a new draft.

## Anonymous scale

Public traffic is anonymous by design. Later capacity controls attach to the edge or the public read API without creating accounts:

- rate limiting
- abuse protection
- caching
- CDN
- bot protection
- search throttling

Redis is present so those controls have a place to land. S1 uses Redis for admin login rate limits and access-token revocation. S3 adds a fail-open cache and a generous limiter for public morphology analysis. Other public reads are not rate limited.

## S6 knowledge

Spelling, rhetoric, literature, and articles are separate modules. They reuse the editorial statuses, the source citation, and the search ports. Literature rights and historical dates are described in ADR-063 and ADR-064. Articles link to other published records through `KnowledgeTargetSource` (ADR-069). Updates record an update audit event rather than a second created event.

## S7 tools

`tools` orchestrates published dictionary, morphology, grammar, search, spelling, and article ports. It does not own the morphology engine and knowledge modules do not depend on it. The public center is `/tools`. See [LINGUISTIC_TOOLS.md](LINGUISTIC_TOOLS.md) and ADR-070.

## S8 assistant

`ai` retrieves published knowledge, then asks `AiModelPort` to phrase an answer from that evidence. Knowledge modules do not depend on `ai`. The assistant is off unless `AI_ENABLED` is true and a configured provider has a key. See [AI_ASSISTANT.md](AI_ASSISTANT.md) and ADR-080.

## SEO

Public knowledge pages are server-rendered and indexable. There is no authentication middleware in the frontend. `/admin` is `noindex` and disallowed in `robots.txt`. Canonical metadata and a one-entry sitemap stay on the homepage. The scaling strategy is in [SEO.md](SEO.md).

## Time and identity

Instants are UTC. Public resource identifiers will be UUIDs, not sequential numbers.

## Optional accounts later

Favorites sync, learning history, exams, certificates, word lists, and personalization may later use an optional account. Basic reading and search remain open if that happens.
