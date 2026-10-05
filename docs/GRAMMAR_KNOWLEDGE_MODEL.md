# Grammar knowledge model

S4 stores Arabic grammar as structured records. A topic is not an HTML article, and a rule is not a single `content` column.

## Records

- `GrammarTopic` is a node in a hierarchy. It has a parent, a category, a display order, a difficulty, and a stable slug.
- `GrammarRule` belongs to one topic. Its statement is `ruleText`, and the rest of the knowledge lives in components.
- `GrammarConcept` is a term, with a short definition, a detailed definition, and aliases stored as rows.
- Components use one type list: `DEFINITION`, `CORE_RULE`, `CONDITION`, `EXCEPTION`, `NOTE`, `WARNING`, `TERMINOLOGY`, `DIFFERENCE`, `SCHOLARLY_NOTE`. Conditions and exceptions are component types, not a second table. An exception can carry its own citations.
- Examples use `CONSTRUCTED`, `QUOTED`, `QURANIC`, `POETRY`, `PROSE`, `COUNTEREXAMPLE`, and `OTHER`.
- Rule relations use `RELATED_TO`, `PREREQUISITE_OF`, `EXCEPTION_TO`, `SPECIAL_CASE_OF`, `CONTRASTS_WITH`, and `SEE_ALSO`.
- Topic prerequisites say which published topic should be known first. They do not start a learning platform.

Categories shown in Arabic are foundations, the nominal sentence, the verbal sentence, the marfuat, the mansubat, the majrurat, the tawabi, the nawasikh, the styles, the numerals, and other. Difficulty is editorial metadata: beginner, intermediate, or advanced. It is not an academic verdict.

## Slugs

A slug is created once with `ContentSlugs`: the normalized title, spaces as hyphens, plus the first eight hex characters of the id. Renaming a record does not change the public URL. A title collision therefore still has a distinct slug.

## Publication

Public pages read the published snapshot and the `published_*` columns. A draft edit of a published record opens a content revision and keeps the previous snapshot. Search matches `published_normalized` and published concept aliases only. Archive clears those columns.

A published rule needs at least one component and one citation. A published concept needs both definitions and one citation. A topic needs a summary. If a topic has citations, every one of them must be publishable. Quoted, Quranic, poetry, and prose examples require a citation. Constructed examples and counterexamples may omit one and are labeled as editorial examples.

Quranic examples store surah (1–114) and ayah. Poetry examples store the poet, the work, and an optional verse locator. S4 does not import the Quran or a poetry corpus.

## Public URLs

- `/grammar`
- `/grammar/{slug}`
- `/grammar/rules/{slug}`
- `/grammar/concepts/{slug}`

`GET /api/v1/public/grammar/search` is a normalized substring search over published titles, terms, and aliases. Page size is 1 to 50. Ranked linguistic search waits for S5.
