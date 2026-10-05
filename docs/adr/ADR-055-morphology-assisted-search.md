# ADR-055: Morphology-assisted search

Status: Accepted

Date: 2026-10-05

## Context

A surface such as `والكتاب` may not be a stored title, while the morphology analyzer can point it at a published entry.

## Decision

Search calls `MorphologySearchAssist`, implemented in the morphology module. It keeps at most five candidates that have a lexical entry id. It runs only when the index has no hit at the root tier or above. Those hits are labeled `MORPHOLOGY`. The analyzer page is unchanged.

## Consequences

Rule-derived noise cannot bury an exact dictionary result. If analysis rejects the input, search still returns the index hits.
