# Grounding policy

An assistant claim about the reference is allowed only when a published evidence item supports it.

## Allowed sources

Published dictionary, morphology, grammar, spelling, rhetoric, literature, articles, and the deterministic tool outputs those modules already expose. Drafts, archived rows, and the model's prior training are not sources.

## What the model may not invent

- a source, page, root, or citation
- a URL
- an evidence id that retrieval did not assign
- a definite morphological fact from a rule-derived candidate
- a semantic difference when no published relation was retrieved

Unknown cited ids are dropped. If none remain, the model prose is discarded and the visitor sees the insufficient-evidence sentence. `grounded` is set by that check, not by a sentence in the completion.

## Insufficient and partial answers

When retrieval finds nothing usable:

**لا تتوفر في المرجع حاليًا معلومات موثقة كافية للإجابة عن هذا السؤال.**

Nearby search links and a relevant tool may still be shown. They are not citations.

A compound question can be partial. A published meaning can be answered while automatic parsing is named as unsupported. A comparison without a published synonym or antonym says the semantic difference is not documented. Those answers are `PARTIALLY_GROUNDED`.

## Evidence is data

The system prompt is fixed (`AR_ASSISTANT_V1`). Evidence is wrapped as untrusted data. Instructions inside an article, a definition, or a prior turn do not become system instructions. Prior turns are truncated, stripped of markup, and are not used to retrieve new evidence.

## Copyright

Context excerpts are short and markup-stripped. Literary works contribute search snippets and metadata, not a long protected passage. The assistant does not publish anything and does not write into a linguistic record.
