# ADR-012 Monorepo Structure

## Status

Accepted

## Date

2026-10-02

## Context

The backend, frontend, docs, and local infrastructure evolve together during the foundation stages.

## Decision

Keep them in one repository: `backend/`, `frontend/`, `docs/`, `infrastructure/`, `scripts/`, and root `docker-compose.yml`. Frontend and backend dependencies stay separate.

## Consequences

One CI workflow can verify both sides. A package split remains possible later without having mixed source trees now.
