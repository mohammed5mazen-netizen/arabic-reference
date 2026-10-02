# ADR-031 Morphological Ambiguity Model

## Status

Accepted

## Date

2026-10-03

## Context

An undiacritized Arabic string can match more than one lemma, clitic split, or pattern. Returning one analysis would hide a real reading.

## Decision

`AnalysisReport` returns `1..N` candidates, ordered manually verified, then exact dictionary, then rule-derived, then by lemma and explanation code. If more than one candidate remains, `resultClass` is `AMBIGUOUS`. The engine does not drop a valid candidate because another one exists. The list is cut at a configured maximum and `truncated` is set.

## Consequences

The public page titles multiple results "تحليلات محتملة". A later ranker can reorder only by changing this explicit order, not by hiding candidates.
