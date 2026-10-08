# ADR-101: Design token system

Status: Accepted

Date: 2026-10-09

## Context

Color and elevation were already named (`--paper`, `--ink`, `--library`), and some meanings such as warning and focus were implied by one-off classes.

## Decision

Keep the existing names and add semantic aliases: background, foreground, card, border, primary, accent, success, warning, destructive, info, focus, radius, and shadow. Light and dark assign the same names. Components do not introduce a second palette.

## Consequences

A theme change is a variable change. Contrast still needs a human check on real screens.
