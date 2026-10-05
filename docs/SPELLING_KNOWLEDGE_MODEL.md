# Spelling knowledge model

Spelling is a structured knowledge module, not a pile of articles and not a spell checker.

## Records

- `SpellingTopic` groups a subject such as hamzat al-wasl or soft alif. The names are editorial data, not a fixed seed.
- `SpellingRule` holds the title, slug, summary, core rule, difficulty, status, version, and editorial metadata.
- `SpellingClause` stores definition, condition, exception, and note as separate rows.
- `SpellingExample` is one of contrast, quoted, constructed, or common mistake.

A contrast that shows another written form must say when that form would still be acceptable. A quoted example and a common mistake require a citation. A constructed example is labeled editorial and may omit a citation.

## Publication

A published rule needs at least one clause and one rule-level citation whose source is published and licensable. Editing a published rule opens a draft and records a revision. The public page keeps the previous snapshot until the next publication.

Public routes: `/api/v1/public/spelling/topics`, `/topics/{slug}`, `/rules/{slug}`, and the same paths on the site.
