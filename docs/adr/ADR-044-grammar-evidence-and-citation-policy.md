# ADR-044: Grammar evidence and citation policy

Status: Accepted

Date: 2026-10-03

## Context

A published grammatical claim needs a source. A quotation with no source should not look like a documented witness. Restricted material must not reach the public page.

## Decision

S4 reuses `LicensePolicy` and source citations. A published rule and a published concept each need at least one citation, and every cited source must be published with a license that allows public attribution. Quoted, Quranic, poetry, and prose examples require a citation at creation. Constructed examples and counterexamples may omit one and carry the editorial label. Quran and poetry locators are stored, but no corpus is imported.

## Consequences

There is no second licensing subsystem. A restricted citation blocks publication of the grammar record.
