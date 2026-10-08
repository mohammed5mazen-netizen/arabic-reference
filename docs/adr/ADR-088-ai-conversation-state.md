# ADR-088: AI conversation state

Status: Accepted

Date: 2026-10-06

## Context

Follow-up questions are useful. A server-side anonymous conversation token is another store of visitor text.

## Decision

S8 is stateless. The client may resend at most two prior turns. They are truncated, treated as untrusted, and omitted from retrieval and from `ai_usage`. No conversation table is created.

## Consequences

A new browser session has no memory. That is the privacy default for this stage.
