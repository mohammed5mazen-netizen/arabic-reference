# ADR-004 Source-First Linguistic Knowledge

## Status

Accepted

## Date

2026-10-02

## Context

A linguistic platform loses trust if statements cannot be traced. The future model is a knowledge graph of words, roots, meanings, morphology, examples, rules, and citations.

## Decision

Linguistic facts must be able to attach to a source, citation, contributor, reviewer, verification state, revision, and publication state. S0 does not build that schema. The `source` and `content` boundaries exist so later stages can.

The editorial lifecycle will be `DRAFT → IN_REVIEW → VERIFIED → PUBLISHED → ARCHIVED`.

## Consequences

Features that generate text, including AI, have to land in this model as proposals or assistants. They do not create published truth by themselves.
