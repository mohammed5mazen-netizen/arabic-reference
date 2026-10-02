# ADR-015 RBAC Strategy

## Status

Accepted

## Date

2026-10-03

## Context

Role names such as `ADMIN` or `EDITOR` will change meaning as the editorial workflow grows. Checking role names inside controllers would freeze that meaning.

## Decision

Authorization checks permissions, not role names. `AuthorizationService.has` is the method-security expression `@authz.has('admin.user.view')`.

Seeded roles are bundles of permissions:

- `PLATFORM_OWNER` holds the full catalog.
- `ADMIN` holds account and role administration, not editorial publishing.
- `EDITOR`, `LINGUISTIC_REVIEWER`, `PUBLISHER`, and `AUDITOR` hold the future editorial permissions they will need.

A staff member may grant a role or add a permission only when they already hold every permission being granted. System roles stay in the catalog. The platform-owner role cannot lose permissions through the API.

The UI hides actions the current session cannot perform. The API still enforces the same rule.

## Consequences

Editorial permissions exist before the content tables. They do not create dictionary or grammar behavior. Adding a real editorial action later means protecting the new service method with an existing permission.
