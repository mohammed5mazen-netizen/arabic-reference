# ADR-086: AI cost and rate limits

Status: Accepted

Date: 2026-10-06

## Context

A model call costs more than a dictionary read, and an anonymous endpoint can be scripted.

## Decision

Cap question size, context items, excerpt length, output tokens, and retrieval fan-out. Rate-limit `POST /api/v1/public/ai/ask` in Redis, default 12 requests per minute per client address. A Redis error does not take the site down; the limiter fails open the same way the tools limiter does. One retry is allowed for a timeout or a provider 5xx. A provider 4xx, including 429, is not retried.

## Consequences

The public assistant stays anonymous. Abuse is limited by request shape and rate, not by an account.
