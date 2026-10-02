# ADR-027 Dictionary Lookup and Normalization

## Status

Accepted

## Date

2026-10-03

## Context

Readers type diacritics and alef variants. S5 will own ranked search. S2 only needs an exact dictionary lookup.

## Decision

Lookup normalizes the query with the S0 `ArabicTextNormalizer` and matches `published_lemma_normalized`. `كِتاب` and `كتاب` meet when they normalize to the same lemma. The original lemma and vocalized form stay in the response. Normalization does not merge two lexical entries. Requests are paginated, and a page size outside 1 to 50 is rejected. There is no Elasticsearch index.

## Consequences

Display never uses the normalized string as the headword. A later search engine can sit behind `LinguisticSearchPort` without changing this exact lookup.
