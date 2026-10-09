# Deployment

The application is provider-neutral. The same images run on a VPS, Fly.io, Railway, Render, or another host that can run a container and reach PostgreSQL and Redis. This document does not bind a vendor and does not contain a real domain.

## Processes

| Process | Role |
| --- | --- |
| Frontend | Next.js production server (`node server.js` from the standalone build). Not `next dev`. |
| Backend | Spring Boot jar, profile `prod`, non-root user in the image. |
| PostgreSQL | Managed database preferred. Flyway migrates on startup. |
| Redis | Managed instance preferred. Required for staff auth limits, the assistant, and quizzes. |

`docker-compose.prod.yml` is the reference layout. It does not publish 5432 or 6379. Application ports bind to `127.0.0.1` so a reverse proxy on the same host terminates TLS.

The frontend image does not receive `DB_*` or `REDIS_*`. It receives `SITE_URL`, `SEO_INDEXING_ENABLED`, and `NEXT_PUBLIC_API_URL`. `NEXT_PUBLIC_*` values are also build arguments because Next inlines them.

## Domain shape

Decide this when the real domain exists. The recommended shape, if nothing else constrains it:

- Public site: `https://DOMAIN`
- API: `https://api.DOMAIN`
- Admin UI: `https://DOMAIN/admin`

One canonical host. If both `www` and the apex answer, redirect one to the other with a permanent redirect at the proxy. Do not invent the domain in `SITE_URL` before it resolves.

`FRONTEND_URL` is the public origin and is the only CORS origin. `SITE_URL` is the canonical origin and must be the same public https origin. `NEXT_PUBLIC_API_URL` is the API origin the browser calls.

## DNS

Create the records the chosen host documents. Typical cases are an A or AAAA record to a VPS, or a CNAME to a platform hostname. Add the API name the same way when the API is a separate service. Do not copy sample IPs into this file as if they were final.

## HTTPS

Production is HTTPS. The proxy or platform redirects HTTP to HTTPS and renews the certificate automatically. Set `ENABLE_HSTS=true` only after HTTPS works. Set `TRUSTED_PROXIES` to the proxy address that connects to the backend so rate limits see the client.

## Secrets

Set them in the host's secret store. Never commit them. Required in production: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`, `ADMIN_JWT_SECRET`, `SITE_URL`, `FRONTEND_URL`. `SEO_INDEXING_ENABLED` stays `false` until the domain is checked. `AI_ENABLED` stays `false` until a key exists outside Git. Optional: `AI_API_KEY`, `AI_PROVIDER`, `AI_MODEL`, `TRUSTED_PROXIES`, `ENABLE_HSTS`, `DB_POOL_SIZE`, `BOOTSTRAP_OWNER_*`, `GOOGLE_SITE_VERIFICATION`.

## Database

The pool defaults to 20 connections, with a 3 second connection timeout. A request that cannot get a connection returns 503 and does not include SQL. `ddl-auto` is `validate`. Flyway migrates and refuses `clean`. A failed migration stops startup. SSL for a managed database is set with `sslmode` on the JDBC URL when the provider requires it. Do not open 5432 or 6379 to the internet.

## First boot

1. Create empty PostgreSQL and Redis.
2. Set secrets and `SPRING_PROFILES_ACTIVE=prod`.
3. Start the backend. Flyway applies V1 onward. The guard exits if a secret is still a local placeholder.
4. Create the owner with `BOOTSTRAP_OWNER_*` set once. The production profile marks `mustChangePassword`. Remove the password from the environment after the first successful start. A later start sees an existing owner and does not create another.
5. Change the password through `POST /api/v1/admin/auth/change-password`.
6. Start the frontend with the real `SITE_URL` and API URL.
7. Leave indexing off until `LAUNCH_CHECKLIST.md` is done.

## CI

GitHub Actions runs backend `verify` and frontend lint, typecheck, build, then test. The workflow file contains no secrets. Tests use Testcontainers, not a production database. Protect `main` later: required CI, no force push. That setting is a GitHub configuration, not a workflow step.
