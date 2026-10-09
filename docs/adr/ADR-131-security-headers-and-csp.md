# ADR-131: Security headers and CSP

Status: Accepted

Date: 2026-10-09

## Context

Public pages and the API need browser hardening without a Next.js middleware. The public-access rule forbids `middleware.ts`, so a per-request nonce is not available. The theme script and JSON-LD are inline.

## Decision

The frontend sends a Content-Security-Policy of `default-src 'self'` with `script-src 'self' 'unsafe-inline'` for the theme script and JSON-LD, self-hosted fonts, and `connect-src` limited to this origin plus `NEXT_PUBLIC_API_URL`. `frame-ancestors 'none'` and `X-Frame-Options: DENY` block framing. The API sends `default-src 'none'`.

HSTS is sent only when `ENABLE_HSTS=true` on the frontend or `app.security.hsts=true` on the backend. Local HTTP does not set it.

## Consequences

Inline scripts are allowed because a nonce middleware is forbidden. `default-src *` is not used. Third-party script and font hosts are not allowed.
