# ADR-089: AI caching and knowledge generation

Status: Accepted

Date: 2026-10-06

## Context

The same normalized question should not call the provider on every request, and a cached answer must not outlive a publication.

## Decision

When `AI_CACHE_ENABLED` is true, Redis stores the structured answer. The key is a SHA-256 of the normalized question, dictionary content stamp, morphology rule generation, search generation, search index version, prompt version, and model identifier. The question text is not the Redis key. Cache failures are ignored and the request continues.

## Consequences

A publish or archive that changes one of those stamps misses the cache. Tests disable the cache so call counts stay stable.
