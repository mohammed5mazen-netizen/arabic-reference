# Manual syntax annotation

S4 records verified sentence annotations. It does not parse a sentence on demand, and it does not present a generated i'rab as if it were reviewed.

## Annotation

`SentenceAnnotation` stores the sentence, its normalized form, an optional citation, and the same editorial lifecycle as a rule. Tokens and dependency edges belong to that annotation. They do not have a separate publication status.

A token stores the surface, the normalized form, a unique position, an optional dictionary entry, an optional morphology analysis, a grammatical role, a case or mood, and an explanation. The editor sets the segmentation. There is no automatic tokenizer.

Grammatical roles are a catalog, not a closed scholarly list. The seed covers the common nominal roles and the verb. A role declares whether it expects a nominal case, a verbal mood, or no check. Nominative and accusative are allowed for both. A nominal role cannot be jussive. A verbal role cannot be genitive.

Dependency edges connect two different token positions with a short label, such as verb to subject. They are manual data. There is no dependency parser.

## What the public page shows

A public annotation is available only after publication. A rule example copies the annotation's published tokens into the rule snapshot. If the example points at an annotation that is not published, the page says: لا يتوفر تحليل نحوي موثق لهذه الجملة.

Dictionary and morphology links are resolved when the annotation is published. A link appears only when the lexical entry or the analysis itself has a published snapshot. The grammar domain reaches those facts through `GrammarCrossLinks`. It does not call dictionary or morphology repositories.

There is no endpoint that claims to parse an arbitrary sentence.
