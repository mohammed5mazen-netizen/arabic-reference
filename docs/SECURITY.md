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

`/api/v1/admin/**` requires authentication. S0 has no user store and no login form, so these routes fail closed with `401` and a JSON body. There is no redirect to a login page.

Content mutations (`POST`, `PUT`, `PATCH`, `DELETE`) on `/api/**` are also closed. The public API is read-only.

## Actuator

`GET /actuator/health` is public and hides component details (`show-details: never`) outside tests. Other actuator endpoints are denied even if they are later exposed by configuration.

## Sessions and CSRF

The API is stateless. CSRF protection is disabled because S0 does not use a browser session cookie for authentication, public methods are safe reads, and mutations are rejected for anonymous clients. S1 must revisit CSRF if admin authentication uses cookies. A header-based admin credential can keep CSRF disabled.

CORS allows the configured `FRONTEND_URL` to call `GET`, `HEAD`, and `OPTIONS` on `/api/v1/public/**` without credentials.

## Secrets and logs

Passwords in Git are limited to the documented local placeholder `local-dev-only`. Production must override `DB_PASSWORD` and `REDIS_PASSWORD`.

Logs include method, path, status, duration, and trace id. They do not include query strings, bodies, passwords, or tokens. Unexpected errors are logged on the server. The client receives a stable code and a generic message, never a stack trace or SQL text.

## Anonymous abuse

Accounts are not the abuse-control mechanism. Rate limiting, throttling, caching, and bot protection can sit in front of the public read API later.

## Optional personal accounts

A future optional account must not be required to read the reference or run basic search.
