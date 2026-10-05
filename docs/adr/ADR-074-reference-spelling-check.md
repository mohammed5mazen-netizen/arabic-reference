# ADR-074: Reference spelling check

Status: Accepted

Date: 2026-10-05

## Context

The spelling knowledge base records correct forms, contrasts, and common mistakes. It is not a general corrector.

## Decision

The tool is named التحقق الإملائي المرجعي. It reports a dictionary hit, a documented form, a recorded mistake, or that the form was not found. A fuzzy neighbor is labeled هل تقصد؟ and is not a confirmed correction. الصحيح هو is not claimed unless a recorded contrast or mistake names that form.

## Consequences

Unknown text stays unknown.
