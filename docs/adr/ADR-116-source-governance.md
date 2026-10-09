# ADR-116: Source governance

Status: Accepted

Date: 2026-10-09

## Context

Sources were already the citation authority. Editors still needed usage, duplicate candidates, Arabic rights labels, and a deletion rule that does not remove a cited work.

## Decision

S11 extends `reference_source` with an identity key and admin queries. Duplicate groups are displayed and never merged. Delete returns 409 when the source is cited or is not a draft. License labels are translated. Age does not decide public domain.

## Consequences

Two different works that normalize to the same title, author, and edition appear as candidates. A person decides whether they are the same.
