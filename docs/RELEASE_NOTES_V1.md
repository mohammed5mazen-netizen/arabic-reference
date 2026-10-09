# Arabic Reference v1.0

Release notes for the candidate built through S13. The git tag is not created in this stage.

## What this is

An Arabic linguistic reference. Visitors read without an account. Staff edit through `/admin`.

## Included

- S0 foundation: modular monolith, PostgreSQL, Flyway, Redis, Next.js.
- S1 staff identity: roles, bearer tokens, refresh rotation, audit, one-time owner bootstrap.
- S2 dictionary and roots.
- S3 bounded morphology.
- S4 grammar topics, rules, and concepts.
- S5 published search.
- S6 spelling, rhetoric, literature, and articles.
- S7 linguistic tools over published knowledge.
- S8 source-grounded assistant, off unless a provider key is set.
- S9 anonymous learning paths, lessons, and quizzes.
- S10 public experience.
- S11 editorial queue, quality rules, and source governance.
- S12 canonical URLs, sitemap, robots, and structured data for published pages.
- S13 production profile, fail-fast secrets, security headers, trusted proxies, health probes, backup and rollback docs, and CI.

## Not included

Learner accounts, payments, a social network, a mobile app, a public file host, analytics, and a hosted search-engine integration. Licensing is not chosen yet. Visual sign-off of every screen is still a manual pass. The production domain is not attached.
