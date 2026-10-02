# ADR-005 Original vs Normalized Arabic Text

## Status

Accepted

## Date

2026-10-02

## Context

Search and comparison need a stable Arabic form, but diacritics, tatweel, and hamza placement are part of the written language. Replacing the original string destroys evidence.

## Decision

`originalText` and `normalizedText` are different values. Normalization never overwrites the original. The S0 normalizer is conservative: it strips harakat and tatweel and folds alef-hamza forms, and it does not fold taa marbuta or alef maksura.

## Consequences

Storage and APIs that appear later must keep both fields when normalization is applied. More aggressive search folding requires a new named profile, not a silent change to the original.
