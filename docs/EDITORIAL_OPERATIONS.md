# Editorial operations

S11 adds an operations room on top of the existing knowledge modules. Dictionary, grammar, morphology, spelling, rhetoric, literature, articles, and learning keep their own records. The `editorial` module coordinates the queue, assignments, internal comments, diffs, and quality scans. It does not own those entities and it does not update them with raw SQL.

The staff entry is `/admin/editorial`. Review, publishing, and quality live at `/admin/editorial/reviews`, `/admin/editorial/publishing`, and `/admin/editorial/quality`. A record page shows the timeline, internal comments, readiness, and diff. Domain publish, verify, and archive stay on the owning module.

## Read model

`editorial_record` is a Postgres view. Counts and the queue are SQL aggregations over that view, joined to the latest reviewer assignment and open blocker findings. The queue can filter by content type, status, creator, reviewer, assignee, updated time, quality state, and a title, slug, or id search. Sort is limited to `updatedAt`, `createdAt`, and `title`. Page size is at most 50.

Morphology rules, search tuning, tool metrics, assistant transcripts, and quiz attempts are not queue items. A morphology analysis is, because it has an editorial status. Learning lessons are inspected through their path.

## Assignment and comments

A record can have one reviewer and one publisher assignee. The assignee must be active and hold `editorial.content.review` or `editorial.content.publish`. The creator cannot be the reviewer. The publisher assignee cannot be the creator or the current reviewer. Creating an assignment uses expected version `0`. A later change must send the stored version. A stale version returns `409`.

Comments are internal. Kinds are `GENERAL`, `CORRECTION`, `SOURCE_REQUIRED`, `RIGHTS_ISSUE`, `STRUCTURE`, `LANGUAGE`, and `OTHER`. A comment is `OPEN` or `RESOLVED`, with `resolvedBy` and `resolvedAt`. Creating a comment requires the current content version. The public API does not return comments or assignments.

## Timeline and diff

The timeline reads `admin_audit_event` and `content_revision` for that record and sorts them by time. Audit metadata on the timeline is limited to status, reason, and license.

A diff names both revisions or neither. Neither compares the published snapshot with the current title, slug, status, and summary. A revision diff loads those two snapshots only. The result uses Arabic labels such as title changed, summary changed, citation removed, and example edited, plus a grapheme diff so Arabic combining marks stay with their base letter. Fields whose names contain password, secret, token, hash, or answer are omitted.

## Bulk

The only bulk actions are reviewer assignment and a quality check, at most 100 records. Publish, delete, and verify are rejected. Each item returns `SUCCESS` or `FAILED` with a reason. The permission is checked before the loop.

There is no email and no live notification channel. Badges are the dashboard counts.
