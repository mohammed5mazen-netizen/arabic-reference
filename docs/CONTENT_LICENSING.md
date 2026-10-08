# Content licensing

The platform does not copy dictionaries, books, or web pages because they are available online. A linguistic statement enters the reference only with source metadata.

Future source records need at least:

- source name
- author
- publisher
- edition
- publication year
- license
- public-domain status
- attribution requirements
- source URL when one exists

Editors must be able to show what may be quoted, what requires attribution, and what must not be stored. AI output is not a license and not a substitute for this metadata.

S2 enforces the license before publication. `PUBLIC_DOMAIN`, `CC0`, `CC_BY`, `CC_BY_SA`, and `PERMISSION_GRANTED` may be published. `RESTRICTED` and `UNKNOWN` may be catalogued and cannot be published or used as the citation of a published sense. A published sense needs at least one citation. There is no production seed of dictionary text. See ADR-029.

S4 uses the same policy for grammar. A published rule or concept needs a citation whose source is published and whose license allows public attribution. A quoted, Quranic, poetry, or prose example needs a citation when it is created. A constructed example may omit one and is labeled as an editorial example. Restricted quotations are not published. See ADR-044.

S6 uses the same citation record for spelling, rhetoric, literature, and articles. A published spelling rule, rhetoric device, and article each need a publishable citation. Quoted spelling examples and common mistakes need one too. Literary excerpts are stricter: they exist only when the work is `PUBLIC_DOMAIN` or `LICENSED`, and never when rights are `UNKNOWN` or `RESTRICTED`. See [LITERATURE_RIGHTS_POLICY.md](LITERATURE_RIGHTS_POLICY.md).

S8 does not add a new license. The assistant may quote only a short published excerpt already allowed by that policy. It does not send a full literary work to a model, and it does not publish model output into the reference. See [AI_GROUNDING_POLICY.md](AI_GROUNDING_POLICY.md).

S9 lessons do not copy the knowledge base. A constructed example is labeled **مثال تعليمي**. A section marked as a quotation cannot be published unless the lesson also points at a published reference. The editor writes quiz answers. The assistant does not invent a citation or a correct option.
