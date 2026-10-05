# ADR-042: Grammar rule component model

Status: Accepted

Date: 2026-10-03

## Context

A rule needs a definition, a statement, conditions, exceptions, and notes. Storing each in its own table would repeat the same fields. Burying them in one paragraph would hide them from later queries.

## Decision

One `grammar_rule_component` table uses `ComponentType`. `CONDITION` and `EXCEPTION` are types in that list. Component citations, especially for an exception, reuse the S2 citation table.

## Consequences

The rule page groups components by type. There is no second condition or exception model to keep in sync.
