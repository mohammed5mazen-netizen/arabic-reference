# ADR-093: Quiz question model

Status: Accepted

Date: 2026-10-09

## Context

Practice questions need a checkable answer without turning the lesson into a game and without asking a model to decide.

## Decision

S9 scores multiple choice, true/false, and multiple select. Multiple select is all or nothing. The score is the integer percentage of fully correct questions. The passing score is 0–100. Matching and classification exist only as lesson activity prompts.

## Consequences

Validation rejects an illegal option set before the question is stored. Scoring stays deterministic and does not call the assistant.
