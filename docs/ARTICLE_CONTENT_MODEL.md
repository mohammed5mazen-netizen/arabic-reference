# Article content model

Articles hold long explanatory writing that does not fit a dictionary entry, a grammar rule, a spelling rule, or a rhetoric device. They do not replace those structured records.

## Records

- `Article` has a title, stable slug, excerpt, type, optional cover label, editor name, status, and version.
- Types are `LINGUISTIC`, `EDUCATIONAL`, `REFERENCE`, `HISTORY_OF_ARABIC`, `TERMINOLOGY`, and `EDITORIAL`.
- `ArticleSection` stores a heading, a body, and a display order.
- Citations attach to the article or to one section, using the shared source citation.
- Tags are normalized names, not a social hashtag system.
- `knowledge_relation` links an article to a published dictionary entry, root, grammar rule, spelling rule, rhetoric device, literary figure, or literary work. There is no database foreign key across those tables. The target is resolved through `KnowledgeTargetSource` and must already be published. Relations are not generated automatically.

Publication requires a section and a citation. The public snapshot keeps sections, tags, sources, and only relations whose targets are still published at publication time. Draft edits do not replace that snapshot.

Public routes: `/articles` and `/articles/{slug}`. Anonymous readers see published articles only.
