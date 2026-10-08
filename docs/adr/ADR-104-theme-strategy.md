# ADR-104: Theme strategy

Status: Accepted

Date: 2026-10-09

## Context

The site already had light and dark variables and a toggle. A late class change can flash the wrong theme.

## Decision

One inline script reads `arabic-reference-theme` or `prefers-color-scheme` and sets the `dark` class before paint. The toggle writes the same key. No account is involved. `suppressHydrationWarning` stays on the document element because the class is chosen on the client.

## Consequences

The first paint follows the saved or system theme. The script is tiny and has no network call.
