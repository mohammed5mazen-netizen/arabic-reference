# ADR-103: Navigation information architecture

Status: Accepted

Date: 2026-10-09

## Context

Putting every module in one bar crowds the header and hides search.

## Decision

Group public links into اللغة, المعرفة, and الخدمات. Search is a field in the header, not one more peer link. المعجم points at `/search` because there is no dictionary index. Unpublished sections are omitted. Desktop uses disclosure groups. Mobile uses one dialog.

## Consequences

New public sections join a group instead of growing a flat list. Admin navigation stays separate.
