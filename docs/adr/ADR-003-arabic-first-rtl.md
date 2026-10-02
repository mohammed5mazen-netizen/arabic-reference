# ADR-003 Arabic-First RTL Architecture

## Status

Accepted

## Date

2026-10-02

## Context

The reference is an Arabic product. Treating Arabic as a translation of an LTR interface produces weak typography and spacing.

## Decision

The document default is `lang="ar"` and `dir="rtl"`. Components use logical CSS properties and RTL-first layout. English LTR can be added later as an additional direction, not as the base.

## Consequences

New UI work starts from Arabic typography, Arabic copy, and RTL spacing. A future locale switch must not require rewriting the page structure.
