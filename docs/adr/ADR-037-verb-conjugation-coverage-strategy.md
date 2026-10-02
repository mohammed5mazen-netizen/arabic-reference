# ADR-037 Verb Conjugation Coverage Strategy

## Status

Accepted

## Date

2026-10-03

## Context

Arabic verb morphology includes sound verbs and several weak classes. Guessing the imperfect vowel of فَعَلَ, or conjugating a hollow verb with sound rules, produces wrong forms.

## Decision

S3 conjugates only a sound triliteral on pattern code `FA3ALA` when a published reading says `VerbClass.SOUND`. Perfect forms are then supported. Imperfect and imperative forms are added only when that reading stores `StemVowel` as fatha, kasra, or damma. Every other class or pattern returns `coverage: UNSUPPORTED` and no forms. The response is HTTP 200.

The imperfect vowel is lexical. It is not inferred from the perfect.

## Consequences

Patterns such as فَعَّلَ can be stored and displayed. They are not conjugated in this version. Weak classes can be recorded for later rules.
