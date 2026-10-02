# S3 Morphology Engine — supported coverage

This is a bounded Arabic morphology foundation. It is not a complete morphological analyzer, and it does not parse sentences.

## Architecture

The `morphology` module has `api`, `application`, `domain`, and `infrastructure` packages. The domain analyzer depends on a `PublishedLexicon` port. An adapter in the application service reads published dictionary rows through `PublishedDictionaryQuery`. Dictionary and source code do not depend on morphology.

Pipeline for `GET /api/v1/public/morphology/analyze`:

1. Reject empty, non-Arabic, and over-long input. The limit is 80 code points.
2. Normalize with the S0 `ArabicTextNormalizer`. The original string stays in the response.
3. Read published lexical entries for the surface and for a small set of clitic segmentations.
4. Attach published manual readings for those entries.
5. Apply enabled code rules.
6. Drop a candidate whose pattern radical count does not equal the published root length.
7. Order candidates deterministically: manual, then dictionary, then rule.
8. Cut the list at `app.morphology.max-candidates` (default 8) and set `truncated` when anything was dropped.

No root is emitted unless a published root record supports it.

## Terminology

| Term | Meaning here |
| --- | --- |
| Surface form | The text the reader typed |
| Lemma | The published lexical headword |
| Pattern | An Arabic template such as فَعَلَ, stored as data with a stable code |
| Features | Structured person, number, gender, aspect, mood, voice, case, definiteness |
| Provenance | Why this candidate exists |

Aspect uses `PERFECT`, `IMPERFECT`, and `IMPERATIVE`. These are not English past and present. Mood names the imperfect's Arabic state: indicative, subjunctive, jussive. Case is stored only when a reading records it. An isolated word does not receive a case.

## Candidate model

`AnalysisReport.analyses` holds one or more candidates. A single candidate is not treated as the only possible reading. When more than one remains, `resultClass` is `AMBIGUOUS`. Each candidate keeps its own provenance:

- `MANUAL_VERIFIED` — a published editorial reading
- `EXACT_DICTIONARY` — a published lemma
- `RULE_DERIVED` — a code rule
- `AMBIGUOUS` — the report as a whole, not a fake probability

There is no numeric confidence.

## Rule engine

Rules are Java classes plus a seeded `morphology_rule` row. Administrators can enable or disable a rule. They cannot submit code. The current set is `s3-sound-2026-10-03`.

| Rule | Effect |
| --- | --- |
| `R-DICT` | Published lemma |
| `R-AL` | Definite article `ال` |
| `R-CONJ` | Conjunction clitics `و` and `ف` |
| `R-PREP` | Proclitics `ب` `ل` `ك` |
| `R-SUFFIX` | A short pronoun list: `ه ك نا كم هم هن ها` |
| `R-FA3IL` | فَاعِل only when that triliteral root is published and has a published verb |
| `R-MAF3UL` | مَفْعُول under the same condition |
| `R-MANUAL` | Published manual reading |

A leading letter is not a prefix unless it is in that inventory and a stem remains. Segmentations are capped.

## Conjugation coverage

| Feature | Support | Note |
| --- | --- | --- |
| Sound triliteral فَعَلَ perfect | Supported | Only when the published reading records class `SOUND` and pattern `FA3ALA` |
| Imperfect and imperative of that verb | Partial | Only when the reading also records the imperfect stem vowel. The vowel is not predicted |
| Form II and other augmented patterns | Unsupported | The patterns exist as catalog data. They are not conjugated |
| Assimilated, hollow, defective, doubled, lafif, hamzated | Unsupported | The class can be stored. No forms are generated |
| Broken plural | Unsupported as a generator | A plural appears only when a published `LexicalForm` of type `PLURAL` exists |
| Sound masculine and feminine plural, dual | Model only | Features can represent them. There is no general generator |
| Full clitic grammar | Partial | The inventory above |
| Diacritization, spelling, syntax | Unsupported | Out of S3 |

`GET /api/v1/public/morphology/conjugate?entryId=` returns `coverage: UNSUPPORTED` and an empty form list when the verb is outside this set. It does not respond with HTTP 500.

Generated cells are marked `RULE_GENERATED`. They are not quotations from a dictionary.

## Provenance and workflow

Manual readings use `DRAFT → IN_REVIEW → VERIFIED → PUBLISHED`, with four-eyes on review and publish. Rule output does not enter that workflow and is not labeled verified. A manual fact can point at an existing S2 citation. Sources are not modeled again.

Generated analyses record `ruleSetVersion`. Redis may cache a public analysis. The key includes the rule-set version, a morphology generation counter, and a dictionary timestamp, so a published entry or a rule change misses the old entry. Redis failure skips the cache. The anonymous limiter defaults to 120 analyses a minute and also fails open.

## Public and admin surfaces

- `GET /api/v1/public/morphology/analyze?word=`
- `GET /api/v1/public/dictionary/entries/{id}/morphology`
- `GET /api/v1/public/morphology/conjugate?entryId=`
- `GET /api/v1/public/morphology/roots/{slug}` shows only patterns recorded on published entries
- `/tools/morphology`
- `/admin/morphology` for patterns, manual readings, rule switches, and the coverage table

The public tool does not require an account.
