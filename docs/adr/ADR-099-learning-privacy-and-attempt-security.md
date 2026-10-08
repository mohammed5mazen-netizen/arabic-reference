# ADR-099: Learning privacy and attempt security

Status: Accepted

Date: 2026-10-09

## Context

A public quiz is easy to scrape if the correct option is in the HTML or the start payload, and an attempt token is a capability.

## Decision

The start payload has option ids and labels only. Explanations and reference links are returned after submit. Tokens are unguessable and hashed at rest. Expiry, idempotency, and a row lock protect submit. Rate limits are aggregate by client address for one minute and are not stored on the attempt. Lesson-view and quiz counters have no title tag. Admin audit records editorial actions, not each student answer.

## Consequences

Someone who has already submitted can replay the same idempotency key and see the result again. They cannot change the recorded score with a second payload.
