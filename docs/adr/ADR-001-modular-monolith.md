# ADR-001 Modular Monolith

## Status

Accepted

## Date

2026-10-02

## Context

The platform will grow across dictionary, grammar, morphology, search, editorial workflow, and learning. Splitting those into services now would add network and deployment cost before the domain model exists.

## Decision

Ship one Spring Boot application with package boundaries per module. Do not introduce microservices in S0.

## Consequences

Modules can later be extracted if a real operational boundary appears. Until then, ArchUnit enforces the in-process boundaries.
