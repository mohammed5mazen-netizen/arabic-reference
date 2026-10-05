# ADR-043: Grammar concept model

Status: Accepted

Date: 2026-10-03

## Context

A term such as i'rab is not itself a rule. It needs a definition, aliases, and links to the rules that use it.

## Decision

`GrammarConcept` is its own aggregate. Aliases are rows in `grammar_concept_alias`, never a comma-separated string. A concept links to rules through `grammar_concept_rule`.

## Consequences

Public search can match a published alias without treating the alias as a second concept. Concept updates reuse the rule workflow permissions rather than adding a parallel permission set.
