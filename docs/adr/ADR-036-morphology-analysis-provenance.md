# ADR-036 Morphology Analysis Provenance

## Status

Accepted

## Date

2026-10-03

## Context

Readers need to see whether a form was checked by an editor, found in the dictionary, or produced by a rule. A percentage would imply a probability the engine does not have.

## Decision

Candidates use `MANUAL_VERIFIED`, `EXACT_DICTIONARY`, `RULE_DERIVED`, or a report-level `AMBIGUOUS`. Conjugated cells use `MANUAL_VERIFIED`, `DICTIONARY`, or `RULE_GENERATED`. The public interface shows Arabic labels such as "موثق يدويًا" and "مستنتج بقاعدة". Rule candidates also carry `appliedRuleIds` and explanation codes.

## Consequences

A generated conjugation is never presented as a quotation. There is no confidence percentage.
