# ADR-073: Knowledge aggregation strategy

Status: Accepted

Date: 2026-10-05

## Context

Word analysis and comparison need dictionary, morphology, relations, grammar, and spelling in one view.

## Decision

The word analyzer is an aggregator. Each lexical entry stays on its own card. Comparison shows the published fields of two words and states a semantic difference only when the forms match or a published synonym or antonym links them. Otherwise it says the reference does not yet document that difference.

## Consequences

Several entries for one word produce the notice وجدنا أكثر من مدخل للكلمة.
