# ADR-105: Shared UI states

Status: Accepted

Date: 2026-10-09

## Context

Empty results, missing pages, and failed requests each had a local sentence. Some failures looked like a harsh error.

## Decision

Use `EmptyState` for no results, `ErrorState` for a failed request with retry, and a dedicated Arabic 404. The assistant's unavailable state stays informational and links to search and tools. Validation errors stay next to the field. No stack traces are rendered.

## Consequences

A network failure in a server page reaches the route error boundary. Pages that can continue without optional data, such as the homepage learning list, catch that failure and still render.
