# ADR-132: Trusted proxy and client address

Status: Accepted

Date: 2026-10-09

## Context

Rate limits use the client address. Trusting `X-Forwarded-For` from the public internet lets a caller rotate that header and bypass the limit.

## Decision

`TRUSTED_PROXIES` is empty by default. The client address is the socket address. `X-Forwarded-For` is read only when the immediate peer is in that list, and only the left-most hop is kept after control characters are removed. Spring's forwarded-header strategy stays unset.

The site is expected to sit behind a TLS reverse proxy. HTTP to HTTPS redirect and certificate renewal belong to that proxy. Set `TRUSTED_PROXIES` to the proxy address that actually connects to the application.

## Consequences

A misconfigured empty list behind a proxy rate-limits the proxy address, which is safer than trusting every client header. CSRF stays disabled because staff auth is a bearer token, not a cookie.
