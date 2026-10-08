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

The staff UI is `/admin/login` and `/admin`. The public header and footer do not link to it. A visual pass must not reveal quiz answers, draft records, or provider keys. The backend remains the authority.

Content mutations on `/api/v1/public/**` stay closed. The public dictionary API is read-only and returns published snapshots only. Admin dictionary and source routes require a staff token and a matching permission. A creator cannot verify their own content, and the reviewer cannot publish it.

## Actuator

`GET /actuator/health` is public and hides component details (`show-details: never`) outside tests. Other actuator endpoints are denied even if they are later exposed by configuration.

## Sessions and CSRF

The API is stateless. Staff authentication uses a bearer access token, not a browser session cookie, so CSRF protection stays disabled. See ADR-014. If a later stage moves the admin credential into a cookie, CSRF must be revisited.

CORS allows the configured `FRONTEND_URL` to call public reads and the admin API. Credentials are not allowed. The admin browser sends `Authorization` instead.

## Secrets and logs

Passwords in Git are limited to the documented local placeholder `local-dev-only` and the local JWT placeholder. Production must override `DB_PASSWORD`, `REDIS_PASSWORD`, and `ADMIN_JWT_SECRET`. `BOOTSTRAP_OWNER_PASSWORD` has no default and is never logged.

Logs include method, path, status, duration, and trace id. They do not include query strings, bodies, passwords, or tokens. Unexpected errors are logged on the server. The client receives a stable code and a generic message, never a stack trace or SQL text.

## Anonymous abuse

Accounts are not the abuse-control mechanism for public reading. Admin login and refresh are rate limited in Redis. Search and the linguistic tools have their own limiters. `POST /api/v1/public/ai/ask` is anonymous and rate limited. The assistant does not log the question or the API key. See [AI_PRIVACY.md](AI_PRIVACY.md).

Quiz start and submit are anonymous and rate limited. The attempt token is random and only its SHA-256 is stored. The public start response does not include which option is correct. Attempt rows are operational data, not admin audit events. See [QUIZ_ENGINE.md](QUIZ_ENGINE.md).

## Optional personal accounts

A future optional account must not be required to read the reference, run basic search, or read a published lesson. Learner accounts are not part of S9.
