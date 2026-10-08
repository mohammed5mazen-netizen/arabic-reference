# ADR-081: Retrieval-first grounding

Status: Accepted

Date: 2026-10-06

## Context

A model can produce fluent Arabic that is not in the reference. Naming the feature RAG does not by itself require embeddings.

## Decision

Classify the question in code, retrieve published records through existing query and search ports, and call the model only when at least one usable evidence item remains. Fuzzy-only hits do not authorize a definite answer. No vector database is added in S8.

## Consequences

Empty retrieval returns the insufficient-evidence sentence without a provider call. A later embedding port would still pass through the same evidence contract.
