# ADR-108: Frontend performance strategy

Status: Accepted

Date: 2026-10-09

## Context

The public site should stay a server-rendered reference. A component framework or a large client graph would compete with the content.

## Decision

Keep Server Components as the default. Client components stay limited to search, navigation, theme, tools, the assistant, quizzes, and local progress. No UI kit was added. Fonts load two families and few weights. The linguistic graph is a list with a compact desktop summary, not a canvas, so it is not lazy-loaded. Images are not part of the public knowledge pages.

## Consequences

LCP is the title and the search field. CLS risk from fonts is reduced with `display: swap` and a reserved header. A future heavy visualization should be a dynamic import.
