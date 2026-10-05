# Arabic search normalization

`ArabicTextNormalizer` is unchanged. Search adds `ArabicSearchNormalizer` in `linguistics.domain.text`.

Three strings stay separate:

- display text, stored as `title_original` and shown to the reader
- the S0 normalized title, stored as `title_normalized`
- the search key, used for equality, prefix, and tokens

## Search key

Starting from the S0 normalized form of each character:

1. Drop punctuation and symbols.
2. Map Arabic-Indic and Eastern Arabic-Indic digits to ASCII digits.
3. Collapse whitespace.
4. Remove one leading `ال` only when at least three letters remain.

`الله` stays `الله` because only two letters would remain. `الكتاب` becomes `كتاب`. `ة`, `ى`, `ء`, `ؤ`, and `ئ` are not folded. Latin text is kept and is not transliterated: `kitab` does not become `كتاب`.

## Phrases

Definitions use `folded`, which is the same pipeline without the leading-article step. A query and a definition are compared after the same fold, so `الشخص الذي يكتب` can match a published definition that contains that phrase. The article is still removed from single-word titles, where `الكتاب` should find `كتاب`.

## Exact versus normalized

Exact match compares the original title with `COLLATE "C"`, so `كتاب` and `كِتَاب` are not the same original string. `كِتَاب` still matches `كتاب` as a normalized exact hit because both keys are `كتاب`.
