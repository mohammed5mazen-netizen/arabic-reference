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
- `/api/v1/admin/**` — editorial staff, closed until S1

The public API must not grow into a content-editing API.

## Backend modules

Base package: `com.mrsoft.arabicreference`

| Module | S0 contents |
| --- | --- |
| `shared` | Errors, time, ids, trace id, security, public foundation endpoint |
| `linguistics` | `ArabicTextNormalizer` |
| `search` | `LinguisticSearchPort` with no adapter |
| `admin` | Closed boundary controller |
| `identity`, `dictionary`, `grammar`, `morphology`, `content`, `source`, `learning`, `ai` | Package boundaries only |

Domain code does not depend on web or persistence. Controllers do not call repositories.

## Linguistic knowledge model

The future core is a knowledge model, not a generic CMS:

`Word → Lemma → Root → Meanings → Morphology → Derivations → Synonyms → Antonyms → Examples → Linguistic Rules → Sources → Citations`

S0 does not create these tables. The module boundaries leave room for them from S2 onward.

Every linguistic fact must be able to point at a source, citation, contributor, reviewer, verification state, revision, and publication state. See ADR-004 and ADR-009.

Editorial lifecycle, documented only:

`DRAFT → IN_REVIEW → VERIFIED → PUBLISHED → ARCHIVED`

## Anonymous scale

Public traffic is anonymous by design. Later capacity controls attach to the edge or the public read API without creating accounts:

- rate limiting
- abuse protection
- caching
- CDN
- bot protection
- search throttling

Redis is present so those controls have a place to land. S0 does not implement them.

## SEO

Public knowledge pages are server-rendered and indexable. There is no authentication middleware in the frontend. Canonical metadata, `robots.txt`, and a one-entry sitemap exist for the homepage. The scaling strategy is in [SEO.md](SEO.md).

## Time and identity

Instants are UTC. Public resource identifiers will be UUIDs, not sequential numbers.

## Optional accounts later

Favorites sync, learning history, exams, certificates, word lists, and personalization may later use an optional account. Basic reading and search remain open if that happens.
