# ADR-013 Public-First Anonymous Access

## Status

Accepted

## Date

2026-10-02

## Context

Arabic Reference is a public linguistic reference, not a private application. Requiring an account before reading would block ordinary use, weaken SEO, and confuse visitors with editorial staff.

## Decision

The site is open. A visitor does not register, log in, or pass through an authentication page to reach the homepage, search, or future public knowledge pages.

Two audiences stay separate:

- **Public visitor** — anonymous. Reads `/` and `GET /api/v1/public/**`.
- **Editorial staff** — future users of `/admin` and `/api/v1/admin/**`.

S0 does not build staff accounts. The admin API is already closed. Public reads are permitted. Content mutations are not public.

Spring Security must not answer a visit or a public read with `401` merely because the caller is anonymous.

## SEO implications

Knowledge pages are HTML documents for crawlers. No authentication middleware stands in front of them. Canonical metadata, robots rules, and sitemaps describe public URLs only.

## Security implications

Anonymous traffic is expected at high volume. Protection is rate limiting, caching, a CDN, bot controls, and search throttling, not a forced account. Health details stay hidden. Admin routes and writes fail closed. Secrets and stack traces stay off the client.

The API is stateless, so CSRF is disabled until an admin cookie session exists. CORS does not send credentials.

## Optional accounts

Favorites, learning history, exams, certificates, word lists, and personalization may later use an optional account. That addition must not change the basic model: reading the reference and basic search work without signing in. Theme and other local preferences use `localStorage` or cookies, not a user record.

## Consequences

S1 is named Admin Identity / RBAC / Editorial Administration. Its authentication is for internal administration only, not for public visitors.

> Arabic Reference is an open linguistic reference. Authentication must never become a prerequisite for ordinary access to public linguistic knowledge.
