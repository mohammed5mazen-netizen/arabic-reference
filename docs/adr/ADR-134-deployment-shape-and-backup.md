# ADR-134: Deployment shape and backup

Status: Accepted

Date: 2026-10-09

## Context

The release candidate must be deployable without choosing a single host, and without putting the database on the public internet.

## Decision

Production is four processes: a Next.js production server, a Spring Boot container, PostgreSQL, and Redis. `docker-compose.prod.yml` keeps database and Redis ports unpublished and binds the apps to loopback. Managed PostgreSQL and Redis are equally valid. The public origin and the API origin are chosen with the real domain later. The recommended shape is `https://DOMAIN` for the site and `https://api.DOMAIN` for the API, with admin remaining on `https://DOMAIN/admin`.

Backups are PostgreSQL dumps. Flyway does not migrate backward. Recovery of bad data is a restore. Recovery of a bad release is a forward fix or a previous image plus a restore only when the schema or data requires it.

## Consequences

No Kubernetes, message broker, or analytics platform is introduced. Licensing stays undecided until the owner chooses. The repository is not switched to public by this stage.
