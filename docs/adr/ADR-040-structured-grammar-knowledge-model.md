# ADR-040: Structured grammar knowledge model

Status: Accepted

Date: 2026-10-03

## Context

A grammar section made of articles (`id`, `title`, `content`) cannot later feed search, exercises, or sentence annotation without scraping HTML.

## Decision

Grammar is a module of topics, rules, typed components, examples, concepts, aliases, and relations. `ruleText` is only the short statement. Conditions and exceptions are component types so the idea is stored once.

## Consequences

Public pages, the admin editor, and later learning features read the same records. Adding a new component type is a controlled enum change, not a new table.
