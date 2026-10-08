# ADR-091: Learning content hierarchy

Status: Accepted

Date: 2026-10-09

## Context

A lesson needs a place in a sequence, a goal, and a short teaching text. A flat article would not show what to study next.

## Decision

The hierarchy is path, unit, lesson, then ordered objectives, sections, references, and activities. A quiz attaches to one lesson or one unit. Display order is contiguous from 1. Difficulty is beginner, intermediate, or advanced, shown in Arabic in the UI.

## Consequences

Publishing a path requires at least one unit, one lesson, one objective, and one section. Empty quizzes are rejected.
