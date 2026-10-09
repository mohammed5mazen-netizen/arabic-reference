# ADR-133: Health, readiness, and Redis failure

Status: Accepted

Date: 2026-10-09

## Context

A dependency blip must not restart the process in a loop, and a Redis outage must not silently drop authentication limits.

## Decision

Liveness is `livenessState` only. Readiness is `readinessState` plus the database. Redis is not a readiness dependency. Health details stay hidden.

Redis failure is fail-closed for admin login, refresh, password change, the assistant, and quiz attempts. Morphology, linguistic tools, and the in-memory search throttle stay fail-open so published reading continues. PostgreSQL remains the source of truth. Cache and rate-limit keys have TTLs.

## Consequences

When Redis is down, staff sign-in, the assistant, and quizzes return 503. Dictionary, grammar, and tool reads continue. A database outage fails readiness and does not fail liveness.
