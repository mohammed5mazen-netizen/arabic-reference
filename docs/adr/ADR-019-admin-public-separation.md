# ADR-019 Admin and Public Security Separation

## Status

Accepted

## Date

2026-10-03

## Context

Adding staff login can accidentally turn the public site into an application that demands an account. ADR-013 already forbids that.

## Decision

The security filter authenticates bearer tokens only on `/api/v1/admin/**`. A token on a public URL is ignored, so a stale admin header cannot turn a public read into `401`.

`GET /` and `GET /api/v1/public/**` stay anonymous. The public pages do not link to `/admin/login`. `robots.txt` disallows `/admin`, and the admin layout is `noindex`.

There is no `/login`, `/register`, social login, or visitor profile.

A staff user with `mustChangePassword` may only read the session, change the password, and log out.

## Consequences

Future public pages stay outside the admin matcher. New staff routes stay under `/api/v1/admin/**` and `/admin`.
