# Arabic text processing

`ArabicTextNormalizer` returns both strings:

- `originalText` — exactly the input
- `normalizedText` — a comparison form

The original is never overwritten. Callers that store text later must store both when a normalized form is useful.

## Conservative profile

Applied only to the normalized copy:

1. Unicode NFC
2. Remove tatweel `U+0640`
3. Remove Arabic harakat and Qur'anic annotation marks
4. Remove bidi controls and BOM
5. Fold alef-with-hamza forms (`أ إ آ ٱ` and a few alef extensions) to bare alef `ا`
6. Map unusual spaces to a normal space and collapse whitespace

Preserved on purpose, because folding them changes meaning:

- taa marbuta `ة` is not folded to haa
- alef maksura `ى` is not folded to yeh
- hamza on waw or yeh stays (`ؤ`, `ئ`, `ء`)

A later profile can add those folds for search if the product decision is explicit. That profile must still keep `originalText`.

## Tests

`ArabicTextNormalizerTest` covers diacritics, tatweel, alef folding, preserved letters, whitespace, bidi marks, NFC, idempotence, empty input, and null rejection.
