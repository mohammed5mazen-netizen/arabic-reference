# ADR-098: Learning AI integration

Status: Accepted

Date: 2026-10-09

## Context

A visitor may ask the assistant about a lesson. A reference question such as "what does this word mean" should still prefer the dictionary or the grammar rule over a lesson paraphrase.

## Decision

`LESSON_HELP` is used when the question names a درس. Retrieval for that intent searches published lessons and paths only and may rank them at the domain score. Every other intent caps lesson and path hits at the search score, below an exact dictionary hit and below grammar-domain evidence. The lesson page links to `/assistant?q=` with the lesson title. Learning does not call a model, and quiz answers are not model output.

## Consequences

The assistant can be disabled and lessons still work. No second provider pipeline is added.
