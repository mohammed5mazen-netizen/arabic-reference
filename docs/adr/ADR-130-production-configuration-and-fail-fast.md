# ADR-130: Production configuration and fail-fast

Status: Accepted

Date: 2026-10-09

## Context

Local development uses placeholder passwords and a localhost database. Those values must not start a production process.

## Decision

`SPRING_PROFILES_ACTIVE=prod` loads `application-prod.yml` and `ProductionStartupGuard`. The guard refuses to start when the database or Redis host is local, when passwords or the JWT secret are missing or still the local placeholders, when `SITE_URL` or `FRONTEND_URL` is not a public https origin, when AI is enabled without a key, or when a bootstrap password is weak. It lists the problem and does not print secret values.

Local and test profiles keep the documented placeholders. Hibernate stays on `validate`. Flyway owns the schema and `clean` is disabled.

## Consequences

A production deploy with a missing secret fails at startup. Staging keeps `SEO_INDEXING_ENABLED=false` until the real domain is verified.
