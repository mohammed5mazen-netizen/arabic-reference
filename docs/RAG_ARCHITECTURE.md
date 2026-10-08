# Retrieval architecture

S8 is retrieval-first. The model sees only the evidence the backend selected. It is not asked to recall Arabic from its own training and then decorate the answer with a source.

## Why there is no vector index

Published lookup already covers the questions this stage answers:

- exact and normalized dictionary match
- root and morphology analysis
- grammar, spelling, rhetoric, and literature search
- full-text and prefix search
- fuzzy search as a nearby hint, not as a basis for a definite answer

That set is enough for a grounded assistant over the current published corpus. A vector database would add a model, an index, and a second source of staleness without a measured retrieval gap. Embeddings are not implemented. There is no unused embedding adapter.

If a later stage shows that lexical retrieval misses paraphrases that staff still want answered, an `EmbeddingPort` can be added beside `KnowledgeRetrievalPort`. The application layer must still select evidence and own the citation contract.

## Retrieval

`PublishedKnowledgeRetrieval` implements `KnowledgeRetrievalPort`. It reads published application ports and `LinguisticSearchPort`. It does not open another module's repositories.

| Intent | First sources | Search filter |
| --- | --- | --- |
| Word meaning, comparison | Dictionary senses and published synonym or antonym relations | Dictionary |
| Root | Root, morphology, dictionary | Root and dictionary |
| Morphology | Morphology analysis, dictionary | Dictionary |
| Grammar | Grammar search | Grammar types |
| Spelling | Reference spelling matches, dictionary | Spelling types |
| Rhetoric, literature | Search snippets only | Matching types |
| General | Unified search | All published types |

Literature and long articles contribute a short search snippet. The context builder does not send a full work.

## Evidence

Each selected item receives a request-local id `E1`, `E2`, `E3`. Those ids are not database ids. The prompt lists them inside `<untrusted-evidence>` and does not include canonical URLs. After the model returns `citedEvidenceIds`, the backend maps each accepted id to the stored path.

Ranking, highest first: exact published knowledge, verified relation, domain record, strong search hit, rule-derived candidate, fuzzy hit. A duplicate `entityType:entityId` is kept once, at the higher score. Fuzzy-only evidence is returned as a nearby link and is not sent to the model. If no item reaches the usable score, the status is `INSUFFICIENT_EVIDENCE` and the model is not called.

## Knowledge freshness

A cached answer key is a SHA-256 digest of the normalized question, the dictionary content stamp, the morphology rule generation, the search generation, the search index version, the prompt version, and the model identifier. Publishing or archiving content changes a stamp, so the previous answer is not reused. The Redis key does not contain the question text.
