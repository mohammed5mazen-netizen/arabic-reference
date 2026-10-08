# ADR-107: Accessibility baseline

Status: Accepted

Date: 2026-10-09

## Context

The reference is Arabic-first and must be usable without a mouse and without relying on color.

## Decision

The baseline is semantic HTML, a skip link, landmarks, one `h1`, visible focus, labeled controls, and text for provenance and progress. ARIA is added for the search combobox, live results, and alerts. `prefers-reduced-motion` is respected. Automated browser audits such as axe were not added because this environment has no browser runner.

## Consequences

`docs/MANUAL_UI_CHECKLIST.md` is the human pass. A later stage can add Playwright without changing this baseline.
