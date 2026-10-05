# ADR-058: Autocomplete strategy

Status: Accepted

Date: 2026-10-05

## Context

Suggestions must be short and fast. Definitions are the wrong source for a dropdown.

## Decision

`GET /api/v1/public/search/suggestions` returns at most 8 titles whose search key equals or prefixes the query: words, roots, grammar concepts, and grammar topics. Exact keys come first, then shorter keys. The client debounces, aborts the previous request, and moves with the arrow keys. Rules and long text are excluded.

## Consequences

The control is a combobox. A suggestion navigates to the published URL.
