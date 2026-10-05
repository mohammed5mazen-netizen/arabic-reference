# Tool result provenance

A tool result names where it came from. The API field `kind` is for clients. The visitor sees `label`.

| Kind | Arabic label | When it is used |
| --- | --- | --- |
| `PUBLISHED_REFERENCE` | موثّق في المرجع المنشور | The result is a published record or an explicit absence |
| `MANUAL_VERIFIED` | موثّق ومراجع | A published manual morphology reading |
| `EXACT_DICTIONARY` | مطابقة معجمية | A published root or lemma match |
| `RULE_DERIVED` | مستنتج بقاعدة | An S3 rule candidate, shown as possible |
| `SEARCH_SUGGESTION` | نتيجة قريبة | A fuzzy neighbor labeled هل تقصد؟ |

`limitations` carries the caution in Arabic: a possible root, incomplete morphology coverage, an insufficient semantic comparison, or the reminder that spelling and grammar tools do not correct or parse free text.

When the result rests on a published page, the payload includes that page URL: `/word/{slug}`, `/root/{slug}`, `/grammar/...`, `/spelling/rules/{slug}`, or `/articles/{slug}`. The tool page does not replace the reference page.

Dynamic tool URLs canonicalize to the tool landing page. See [SEO.md](SEO.md) and ADR-079.
