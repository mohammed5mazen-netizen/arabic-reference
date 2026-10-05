# ADR-064: Literature rights model

Status: Accepted

Date: 2026-10-05

## Context

An excerpt can harm a copyright holder even when the surrounding page is only metadata.

## Decision

Each work has `PUBLIC_DOMAIN`, `LICENSED`, `RESTRICTED`, or `UNKNOWN`, plus a rights note. Excerpts require public-domain or licensed status, a citation, and at most 800 code points. Rights are an editorial decision, not a calculation from the year. Unknown and restricted works show no excerpt.

## Consequences

The policy is written in `docs/LITERATURE_RIGHTS_POLICY.md`. Production migrations do not import literary texts.
