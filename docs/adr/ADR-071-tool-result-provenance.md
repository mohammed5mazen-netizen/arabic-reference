# ADR-071: Tool result provenance

Status: Accepted

Date: 2026-10-05

## Context

A tool can mix a published record, a manual reading, a rule, and a search neighbor. Showing them as one confident answer would overstate the evidence.

## Decision

Every tool envelope carries provenance notes and limitations. Kinds are `PUBLISHED_REFERENCE`, `MANUAL_VERIFIED`, `EXACT_DICTIONARY`, `RULE_DERIVED`, and `SEARCH_SUGGESTION`. The visitor sees the Arabic label, not the enum.

## Consequences

A missing field stays missing. The UI does not fill it.
