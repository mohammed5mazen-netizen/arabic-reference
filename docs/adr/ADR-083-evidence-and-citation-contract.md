# ADR-083: Evidence and citation contract

Status: Accepted

Date: 2026-10-06

## Context

Free-form model prose can cite a page that retrieval never found.

## Decision

The model returns JSON with `answer`, `citedEvidenceIds`, and `limitations`. Parser is Jackson, not a citation regex. Only ids present in the selected evidence are accepted. The backend maps those ids to canonical paths. Invented ids are removed. If no cited id remains, the model answer is discarded.

## Consequences

`grounded` means the citation check passed. The user interface never treats a model sentence as proof of grounding.
