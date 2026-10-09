# Content quality

Quality checks are deterministic Java rules in `QualityRules`. They are not model judgments. A finding has a code, severity (`INFO`, `WARNING`, or `BLOCKER`), message, field, content type, content id, and detection time.

## Universal rules

The shared rules cover a missing title, a summary shorter than 12 characters when a summary is required, a missing citation when the status requires one, a broken source reference, a duplicate slug, an orphan relation, an unpublished relation target, an unknown workflow status, a published row without a snapshot, and a searchable published row without a search document.

## Domain rules

Dictionary checks an entry without a sense, a sense without a citation, and a broken root. Grammar checks a verified or published rule without components and a quoted example without a citation. Spelling checks a published rule without a citation and a common mistake without evidence. Literature checks missing rights and an excerpt on `UNKNOWN` or `RESTRICTED`. Articles check a verified or published article without sections. Learning checks a lesson without an objective, an invalid question, and a knowledge reference that is no longer published. Learning paths also receive an informational review note.

## Scans and findings

`POST /api/v1/admin/editorial/quality/scans` accepts `RECORD`, `TYPE`, or `PUBLISHED`. The request is synchronous and limited by `app.editorial.scan-limit` (default 200, maximum 500). The latest findings are stored on `quality_finding` as `OPEN` or `STALE`. A rescan marks previous open findings for the scanned records as stale, then inserts the current ones. One audit event, `QUALITY_SCAN_RUN`, records the scan. Individual findings are not audit events.

A published-scope scan also asks the existing search ports whether a published document is missing from the index. That surfaces `SEARCH_INDEX_INCONSISTENCY` and per-record `SEARCH_DOCUMENT_MISSING`. It does not retune ranking.

S12 adds two discoverability findings. `INVALID_CANONICAL_SLUG` is a warning when a published slug is blank or contains whitespace, `%`, `?`, `#`, or `/`. `DISCOVERABILITY_ORPHAN` is informational when a published dictionary entry has no root and no relation. A description is still derived from the published summary. It is not a required manual SEO field.

If the catalog grows past a single request, the next step is a database job and a worker in this process. There is no message broker in S11.

An open blocker also stops domain publish through `PublicationChecks`. The domain's own publish rules still run first. Clearing the data and leaving the old finding open keeps publication blocked until a rescan marks it stale.
