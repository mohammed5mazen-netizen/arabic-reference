# Dictionary model

S2 stores an Arabic linguistic knowledge core. A word is not a meaning, and a lemma is not a unique identity.

## Surface form

The text a reader may type, including diacritics and spelling variants. Lookup normalizes it. Display keeps the stored original.

## Lemma

The citation form of a lexical entry, stored as `lemmaOriginal` and `lemmaNormalized`. The same lemma may exist more than once, for example as a noun and as a verb, so `lemmaNormalized` is indexed and not unique.

## Lexical entry

`LexicalEntry` is the only entry aggregate. There is no second Lexeme entity.

It holds the lemma, an optional vocalized form, an optional root, a controlled part of speech, an optional gender, editorial status, a stable slug, actors, and an optimistic version.

## Root

A root is its own record: original spelling, normalized letters, radical count, and optional notes. Triliteral and quadriliteral roots are the normal cases. Lengths 2, 5, and 6 require an editorial note. A root is optional, so loanwords, proper names, and unresolved cases do not need one. Roots are unique on the normalized letters.

## Sense

Each meaning is a `LexicalSense` of one entry. It has a definition, an optional short definition, a controlled usage label, a controlled domain label, and `displayOrder`. Order does not come from the identifier or the creation time. `عين` can have several senses without copying the entry.

## Forms

`LexicalForm` holds vocalized, plural, singular, feminine, masculine, and alternate spellings. Full conjugation is not stored here.

## Relations

Synonyms, antonyms, and related meanings connect two senses. `DERIVED_FROM` connects two entries and records a known derivation. It does not generate morphology. Each relation can carry a verification level and citations. Symmetric pairs are unique. A relation does not point at itself.

## Examples

`UsageExample` belongs to a sense. `EDITORIAL` is written for the reference. `QUOTED` must point at a citation.

## Sources and citations

`ReferenceSource` is the work: type, bibliographic metadata, license, and attribution text. `SourceCitation` is a place in that work. Pages are optional because a website may have none. A page range cannot run backwards.

Evidence uses explicit foreign keys: `sense_citation`, `entry_citation`, `relation_citation`, `root_citation`, and `usage_example.citation_id`. There is no polymorphic target column.

Several citations may support one sense. The sense is not duplicated for each source.

## Revisions

`content_revision` stores a snapshot, the actor, the time, and an optional reason for a main record. That history is separate from the append-only audit event, which keeps only short identifiers and status codes.

## Public visibility

The public API reads `published_snapshot` and hides `ARCHIVED`. A later edit moves the working copy back to `DRAFT` and keeps the last published snapshot, so the public page stays on the last published text and does not leak the draft.
