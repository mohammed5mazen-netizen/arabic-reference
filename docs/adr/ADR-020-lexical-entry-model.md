# ADR-020 Lexical Entry Model

## Status

Accepted

## Date

2026-10-03

## Context

A table of word plus meaning cannot represent Arabic lexicography. The same written word can be more than one entry, and one entry can have many meanings.

## Decision

The aggregate is `LexicalEntry`. Surface text is input. The lemma is stored original and normalized. A root is optional. Part of speech is a controlled lexicographic vocabulary, finer than اسم / فعل / حرف, and it is not a grammar engine. Gender is optional. `lemmaNormalized` is indexed and not unique. The public slug is the normalized label plus the first eight hex characters of the UUID.

## Consequences

Later morphology can attach to the entry. It must not collapse two entries because their normalized lemmas match.
