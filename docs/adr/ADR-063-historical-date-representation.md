# ADR-063: Historical date representation

Status: Accepted

Date: 2026-10-05

## Context

Literary dates are often a year, an approximation, or unknown, and they may be Hijri.

## Decision

`HistoricalDate` stores precision (`EXACT`, `YEAR`, `APPROXIMATE`, `UNKNOWN`), calendar (`GREGORIAN`, `HIJRI`, `UNSPECIFIED`), an optional year, an optional exact date, display text, and a circa flag. Approximate and circa values are labeled نحو سنة. Hijri and Gregorian values are not converted.

## Consequences

A missing date is shown as التاريخ غير معروف. Editors enter the calendar they mean.
