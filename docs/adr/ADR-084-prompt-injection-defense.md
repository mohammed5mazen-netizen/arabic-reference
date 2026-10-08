# ADR-084: Prompt injection defense

Status: Accepted

Date: 2026-10-06

## Context

Published definitions and articles are untrusted text. A record can contain "Ignore previous instructions" without being an instruction to the assistant.

## Decision

The system prompt is a constant, `AR_ASSISTANT_V1`. Evidence is placed in a delimited data block and is not concatenated into the system prompt. The prompt tells the model to ignore orders inside that block. Excerpts are length-capped and markup-stripped before they are stored on the evidence object. Prior turns get the same plain-text treatment and do not drive retrieval.

## Consequences

Changing the prompt requires a new prompt version so cached answers are not mixed with the old instruction.
