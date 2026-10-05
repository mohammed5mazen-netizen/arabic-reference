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

S7 tool input uses the same normalizer after trim. A tool accepts one Arabic word, or a short phrase where the tool definition allows it. Text past the code-point or word limit is rejected. The original display string stays in the response beside `normalizedInput`.

S2 dictionary lookup uses this same normalized form against `published_lemma_normalized`. S3 morphology analysis uses it before segmentation and dictionary matching. The page still renders `lemmaOriginal` and the vocalized form. The normalized string is not a display form and does not merge two lexical entries. A later profile can add more folds for ranked search if that decision is explicit. That profile must still keep `originalText`. Shadda is removed with the other harakat, so pattern identity uses the stable pattern code rather than the normalized skeleton.

## Tests

`ArabicTextNormalizerTest` covers diacritics, tatweel, alef folding, preserved letters, whitespace, bidi marks, NFC, idempotence, empty input, and null rejection.

Search uses a second step, `ArabicSearchNormalizer`, described in `docs/ARABIC_SEARCH_NORMALIZATION.md`. It does not change the S0 profile. Display text stays on `originalText`.
