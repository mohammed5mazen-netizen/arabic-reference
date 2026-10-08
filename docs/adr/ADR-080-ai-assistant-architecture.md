# ADR-080: AI assistant architecture

Status: Accepted

Date: 2026-10-06

## Context

Visitors can already read published dictionary, grammar, spelling, rhetoric, literature, articles, search, and tools. They still have to choose the module. A general chat model would answer outside that corpus.

## Decision

Add an `ai` module with `api`, `application`, `domain`, and `infrastructure`. The application asks `KnowledgeRetrievalPort` first and `AiModelPort` second. Knowledge modules do not depend on `ai`. The root `ai` package stays a package marker. `learning` stays unimplemented.

## Consequences

ArchUnit keeps `ai.domain` free of Spring, JPA, and provider clients, and keeps knowledge modules free of `ai`. The public site works when the assistant is disabled.
