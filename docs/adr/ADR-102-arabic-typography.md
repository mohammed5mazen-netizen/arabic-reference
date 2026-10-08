# ADR-102: Arabic typography

Status: Accepted

Date: 2026-10-09

## Context

Arabic reading needs a text face and a display face, a limited weight set, and a line length that is not the full viewport.

## Decision

Amiri is the display face (400, 700). IBM Plex Sans Arabic is the UI face (400, 500, 600). Both come from `next/font` with `display: swap`. Fallbacks are Noto Naskh Arabic and Segoe UI. Body copy uses the `.reading` measure. Public counts use Latin digits.

## Consequences

The font budget stays small. A future English interface can reuse the same tokens with a different direction.
