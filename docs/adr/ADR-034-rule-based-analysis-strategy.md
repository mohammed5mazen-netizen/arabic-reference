# ADR-034 Rule-Based Analysis Strategy

## Status

Accepted

## Date

2026-10-03

## Context

An open-ended chain of conditions would be impossible to test, and an admin scripting engine would be unsafe.

## Decision

Rules are deterministic Java units with ids and explanation codes. `morphology_rule` stores only an enabled flag, a description, and the rule-set version. Administrators may enable or disable a rule. They cannot upload code. Clitics come from a fixed short inventory. فَاعِل and مَفْعُول are proposed only when a published root and a published verb already exist.

## Consequences

New Arabic phenomena need a new tested rule and a rule-set version change. S3 has no machine-learned ranker.
