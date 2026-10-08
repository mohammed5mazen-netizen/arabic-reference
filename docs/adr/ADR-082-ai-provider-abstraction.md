# ADR-082: AI provider abstraction

Status: Accepted

Date: 2026-10-06

## Context

The assistant must not be rewritten when the model host changes, and tests must not call a paid API.

## Decision

`AiModelPort` is the only generation port. `AI_PROVIDER=openai-compatible` uses `java.net.http` against a chat-completions URL. Tests bind a stub adapter with `AI_PROVIDER=stub`. The application layer does not import a vendor SDK. `AI_API_KEY` comes from the environment. A missing key leaves the assistant unavailable.

## Consequences

Another compatible host is a configuration change. A different protocol needs a new infrastructure adapter, not an application rewrite. `clean verify` does not need the internet.
