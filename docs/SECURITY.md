# Security

## Public visitors

Visitors are anonymous. Opening the site, reading public pages, and calling public read APIs must not return `401 Unauthorized` because nobody is logged in.

S0 does not implement:

- registration
- public login
- social or Google login
- password reset for visitors
- profiles or dashboards
- JWT for the public
- account favorites
- subscriptions

## Staff boundary

`/api/v1/admin/**` is for editorial staff. Anonymous calls receive `401` and a JSON body, with no redirect. Two routes are open so staff can obtain a token:

- `POST /api/v1/admin/auth/login`
- `POST /api/v1/admin/auth/refresh`

A valid token without the required permission receives `403`. That is different from `401`. A method the route does not support, such as writing to the audit log, receives `405` with `METHOD_NOT_ALLOWED`. The response does not include a stack trace.

`robots.txt` and `noindex` are hints to crawlers. They are not the access control for `/admin` or `/api`. S12 does not add analytics cookies or a third-party tracker. `GET /api/v1/admin/seo/status` requires `seo.admin.view` and returns no secrets.

The staff UI is `/admin/login` and `/admin`. The public header and footer do not link to it. A visual pass must not reveal quiz answers, draft records, or provider keys. The backend remains the authority.

Content mutations on `/api/v1/public/**` stay closed. The public dictionary API is read-only and returns published snapshots only. Admin dictionary and source routes require a staff token and a matching permission. A creator cannot verify their own content, and the reviewer cannot publish it. S11 keeps comments, assignments, quality findings, and diffs on `/api/v1/admin/editorial/**`. Anonymous access is `401`. A missing permission is `403`. A stale assignment, comment, or content version is `409`. Bulk publish, delete, and verify are forbidden. The assistant cannot verify, publish, or decide a license.

## Actuator

`GET /actuator/health` is public and hides component details (`show-details: never`) outside tests. Other actuator endpoints are denied even if they are later exposed by configuration.

## Sessions and CSRF

The API is stateless. Staff authentication uses a bearer access token, not a browser session cookie, so CSRF protection stays disabled. See ADR-014. There is no auth cookie to mark `Secure` or `HttpOnly`. If a later stage moves the admin credential into a cookie, CSRF and cookie flags must be revisited.

CORS allows only the configured `FRONTEND_URL`. Credentials are not allowed, and `*` is not used. The admin browser sends `Authorization` instead.

## Production profile

`SPRING_PROFILES_ACTIVE=prod` runs `ProductionStartupGuard` before the application accepts traffic. Missing or local database passwords, Redis passwords, JWT secrets, and non-https public origins abort startup. The guard does not print secret values. See ADR-130.

Owner bootstrap runs only when `BOOTSTRAP_OWNER_*` is complete and no platform owner exists. A second start does not create another owner. In the production profile the new owner has `mustChangePassword`. Change it with `POST /api/v1/admin/auth/change-password`, which is rate limited. Until that change, other admin routes return 403 `PASSWORD_CHANGE_REQUIRED`. Session and the change-password route stay available. Remove the bootstrap password from the environment after the first start.

## Headers and client address

API responses send `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy`, `X-Frame-Options: DENY`, and `Content-Security-Policy: default-src 'none'`. HSTS is off unless `app.security.hsts` is true. The frontend policy is in ADR-131. `unsafe-inline` scripts exist because theme initialization and JSON-LD are inline and `middleware.ts` is forbidden.

`X-Forwarded-For` is ignored unless the socket peer is listed in `TRUSTED_PROXIES`. See ADR-132.

## Redis failure

Login, refresh, password change, the assistant, and quiz attempts fail closed when Redis is unavailable. Morphology, tools, and the in-memory search throttle fail open so reading continues. See ADR-133.

## Secrets and logs

Passwords in Git are limited to the documented local placeholder `local-dev-only` and the local JWT placeholder. Production must override `DB_PASSWORD`, `REDIS_PASSWORD`, and `ADMIN_JWT_SECRET`. `BOOTSTRAP_OWNER_PASSWORD` has no default and is never logged.

Logs include method, path, status, duration, and trace id. They do not include query strings, bodies, passwords, or tokens. Unexpected errors are logged on the server. The client receives a stable code and a generic message, never a stack trace or SQL text.

## Anonymous abuse

Accounts are not the abuse-control mechanism for public reading. Admin login, refresh, and password change are rate limited in Redis and fail closed. Search uses an in-memory throttle. Morphology and tools fail open if Redis is down. `POST /api/v1/public/ai/ask` and quiz attempts are anonymous, rate limited, and fail closed. The assistant does not log the question or the API key. See [AI_PRIVACY.md](AI_PRIVACY.md).

Quiz start and submit are anonymous and rate limited. The attempt token is random and only its SHA-256 is stored. The public start response does not include which option is correct. Attempt rows are operational data, not admin audit events. See [QUIZ_ENGINE.md](QUIZ_ENGINE.md).

## Optional personal accounts

A future optional account must not be required to read the reference, run basic search, or read a published lesson. Learner accounts are not part of S9.
