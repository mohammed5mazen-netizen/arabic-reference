# ADR-032 Arabic Morphological Feature Model

## Status

Accepted

## Date

2026-10-03

## Context

A single feature string cannot grow into person, number, gender, aspect, mood, voice, case, and definiteness without breaking stored readings.

## Decision

Features are a structured record. Number includes singular, dual, and plural. Gender may be masculine, feminine, or not applicable. Verb aspect is perfect, imperfect, or imperative, which names the Arabic categories rather than English tense. Mood is indicative, subjunctive, jussive, or not applicable. Case is nominative, accusative, or genitive, and stays null when the word is analyzed outside a sentence. Definiteness includes definite, indefinite, and construct state. Null means "no evidence", not a default value.

## Consequences

S3 does not generate every Arabic combination. The record can hold them when a later stage has evidence.
