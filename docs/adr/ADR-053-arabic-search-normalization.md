# ADR-053: Arabic search normalization

Status: Accepted

Date: 2026-10-05

## Context

Lookup needs more folding than the conservative S0 normalizer, but folding the wrong letters changes meaning. The S0 normalizer is already used by the dictionary and morphology.

## Decision

`ArabicSearchNormalizer` sits on top of `ArabicTextNormalizer` and does not change it. It drops punctuation, maps Arabic-Indic digits, and strips one leading `ال` only when three letters remain. It does not fold `ة`, `ى`, `ء`, `ؤ`, or `ئ`, and it does not transliterate Latin. Definition text uses the same fold without the article step. Details are in `docs/ARABIC_SEARCH_NORMALIZATION.md`.

## Consequences

`الكتاب` finds `كتاب`. `الله` stays intact. `kitab` stays `kitab`.
