# ADR-014 Admin Authentication Strategy

## Status

Accepted

## Date

2026-10-03

## Context

Editorial staff need to sign in. Visitors do not. A cookie session would bring CSRF back and could be attached to public pages by mistake.

## Decision

Staff authentication is a bearer access token on `/api/v1/admin/**`. The token carries only the staff user id, a token id, issued-at, and expiry. It does not carry roles, permissions, email, or a password.

Permissions and account status are loaded from PostgreSQL on each request. A permission change, a disable, a lock, or `mustChangePassword` applies on the next request rather than waiting for the access token to expire. Logout also places the access token id on a Redis denylist until the token would have expired.

Access tokens last 10 minutes by default. They are signed with HMAC-SHA256. `ADMIN_JWT_SECRET` must be at least 32 bytes and has no production default.

Passwords are hashed with Spring Security's Argon2id encoder (`Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8`) and Bouncy Castle. The hash never leaves the server. The password policy requires 12 to 128 characters with upper, lower, digit, and a special character.

CSRF stays disabled because the credential is the `Authorization` header, not a cookie.

## Consequences

The admin browser stores tokens in `sessionStorage`. XSS on the admin origin can read them, so the admin UI must stay small and free of untrusted HTML. A future cookie design must add CSRF protection before it replaces this header.
