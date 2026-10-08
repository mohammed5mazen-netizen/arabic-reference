# Search architecture

Public search is one box. It reads a published projection, not the editorial tables.

## Boundaries

`search.domain` owns `LinguisticSearchPort` (read), `SearchIndex` (write), `PublishedSearchSource`, and `MorphologySearchAssist`. Dictionary and grammar implement `PublishedSearchSource` and call `SearchIndex` when a record is published or archived. Morphology implements the assist port. The search application never imports those modules' repositories.

The PostgreSQL adapter is `PostgresLinguisticSearchAdapter`. A later `OpenSearchSearchAdapter` can implement the same ports. S5 does not add an Elasticsearch or OpenSearch client.

## What is indexed

Published dictionary entries, roots, grammar topics, rules, and concepts. Citations, annotations, and morphology patterns are not results. Patterns are not on the editorial publish lifecycle, so they are not public hits.

Draft edits do not touch the index. Publish writes the document from the published snapshot. Archive deletes it. Republish replaces it. Rebuild reads the same snapshots, so a draft sitting on top of a still-public snapshot is indexed from that snapshot.

## Transactions

Index writes run in the publisher's transaction. PostgreSQL makes the content change and the index change commit or roll back together.

Both publish and rebuild take transaction-scoped advisory lock `54051`. Publish calls `pg_advisory_xact_lock` before it locks the content row, so a rebuild cannot deadlock on that row. Rebuild calls `pg_try_advisory_xact_lock` and returns 409 when the lock is busy. Two rebuilds therefore yield one success and one conflict. A publish that starts during a rebuild waits, then upserts after the rebuild commits.

Public reads use `SET LOCAL statement_timeout = '5000'` inside the request transaction. That is a local bound, not a distributed timeout.

## Queries

Important shapes, and the indexes that match them:

- title equality and prefix: btree `ix_search_document_key`, plus `COLLATE "C"` for the original-title comparison so harakat stay distinct
- alias and form equality: btree `ix_search_token_key`
- definition contains: GIN `ix_search_document_text_trgm` (`pg_trgm`)
- fuzzy similarity: GIN `ix_search_document_key_trgm`

Candidate retrieval is capped at 200 rows. Fuzzy is capped at 5 and runs only when fewer than 3 stronger hits exist and the key is at least 4 code points, with similarity at least 0.45. Morphology assist is capped at 5 entry ids and runs only when nothing scored at the root tier or above. Results are projected from `search_document`; the page does not query once per hit.

## Operations

`POST /api/v1/admin/search/reindex` rebuilds from published snapshots. It is idempotent. `POST /api/v1/admin/search/repair` rebuilds only when the consistency check finds a published record without a document, a document whose record is no longer public, or a document on an older `indexVersion`. Both require `search.reindex`. Status requires `search.admin.view`.

`indexVersion` is 3 after S9 added learning-path and lesson documents. Changing normalization or the document shape requires a bump and a rebuild. A lesson is indexed from its title and summary, not from the full section body, so a lesson paraphrase does not outrank an exact dictionary title. Equal scores break ties by type priority, and `DICTIONARY_ENTRY` stays ahead of `LESSON`.

S6 types keep the old tie-break order and then follow it: spelling rule, rhetoric device, literary figure, literary work, article, spelling topic, rhetoric topic, literary era. An exact dictionary title still ranks above an equal-score title of a later type, and above an article body match. Suggestions may offer a spelling rule, a rhetoric device, or a literary figure, including a published alias. They do not read article bodies. The public filter `content` groups those four areas. Rebuild and repair include every published S6 snapshot.

## Privacy, metrics, cache

Public searches are anonymous and are not written to the audit log. Micrometer records request count, zero-result count, duration, result count, and suggestion count. The raw query is not a metric tag and is not logged.

The result cache is process-local, at most 64 entries, for 20 seconds. The key includes the in-process generation, `indexVersion`, the display query, the normalized key, filters, page, and size. The display query stays in the key because an exact original match is not the same as a normalized match. Every index write bumps the generation. Redis remains available for a later shared cache; S5 correctness does not depend on it.

Anonymous search is rate-limited in memory, default 600 requests per minute per client address. It does not require a login.

## Assistant retrieval

The linguistic assistant uses this index as its general fallback and as the snippet source for rhetoric, literature, and articles. Direct dictionary, grammar, spelling, and morphology ports still run first for those intents. Fuzzy hits are nearby links, not evidence for a definite answer. See [RAG_ARCHITECTURE.md](RAG_ARCHITECTURE.md).
