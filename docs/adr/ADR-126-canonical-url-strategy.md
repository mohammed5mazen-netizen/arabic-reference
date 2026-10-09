# ADR-126: Canonical URL strategy

Status: Accepted

Date: 2026-10-09

## Context

Canonicals were concatenated per page, and an already encoded Arabic segment could be encoded again.

## Decision

`publicMetadata` builds one absolute canonical from the normalized origin and `canonicalPath`. A segment is decoded at most twice, then encoded once. Query and fragment are stripped. Titles use the layout template `{name} | المرجع العربي` and stay natural.

## Consequences

Duplicate canonicals are a defect. Duplicate titles are allowed when two senses share a heading. See `docs/PUBLIC_URL_POLICY.md`.
