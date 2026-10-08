# ADR-100: Public experience architecture

Status: Accepted

Date: 2026-10-09

## Context

S0–S9 shipped many public sections. They share a language and a stack, and they still read as separate pages.

## Decision

S10 is a frontend experience pass. Domain rules, scoring, search ranking, and editorial workflow stay as they are. Shared layout, navigation, tokens, and states live in the Next.js app. No new backend module and no migration.

## Consequences

Public pages keep anonymous access. A later editorial stage can reuse the tokens without inheriting a new linguistic model.
