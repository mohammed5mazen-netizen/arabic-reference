# Rhetoric knowledge model

Rhetoric is structured knowledge. The foundation categories are علم المعاني, علم البيان, and علم البديع, and the taxonomy can grow through `OTHER`.

## Records

- `RhetoricTopic` is a subject inside a category.
- `RhetoricDevice` is the device: name, slug, short definition, detailed explanation, status, and version.
- Components hold definition, characteristic, type, condition, difference, and note separately.
- Examples carry the text, explanation, highlighted segment, interpretation, scholarly note, and an alternative interpretation.
- Relations are `RELATED_TO`, `CONTRASTS_WITH`, `TYPE_OF`, `OFTEN_CONFUSED_WITH`, and `SEE_ALSO`. A device cannot relate to itself.

An interpretation is not presented as the only fact. The public page shows a reading and, when the editor stored one, another reading. A quoted example requires a citation. A published device needs a component and a device-level citation.

Public routes: `/rhetoric`, `/rhetoric/{slug}`, and `/rhetoric/devices/{slug}`.
