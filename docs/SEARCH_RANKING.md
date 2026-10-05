# Search ranking

The score orders results. It is not shown in the API or the page.

| Reason | Score | Meaning |
| --- | --- | --- |
| `EXACT` | 900 | Original title matches the query |
| `NORMALIZED_EXACT` | 800 | Search key matches the title key |
| `WORD_FORM` | 700 | A published lexical form matches |
| `ALIAS` | 690 | A published alias matches |
| `ROOT` | 600 | A published root key matches |
| related entry | 580 | A dictionary entry of that root, capped at 3 |
| `TITLE_PREFIX` | 500 | The title key starts with the query |
| `MORPHOLOGY` | 400 | A capped analyzer candidate points at a published entry |
| `DEFINITION` | 300 | The folded query occurs in the searchable text |
| `FUZZY` | 200 | Title similarity is at least 0.45 |
| fuzzy body | 100 | Only the body is that similar |

Tie break, in order: score descending, type priority (`DICTIONARY_ENTRY`, `ROOT`, `GRAMMAR_CONCEPT`, `GRAMMAR_RULE`, `GRAMMAR_TOPIC`), normalized title, id.

The same query and the same index therefore return the same order.

Morphology runs only when no hit has reached the root score or higher, and a generated candidate never outranks an exact dictionary title. Fuzzy runs only for keys of at least 4 code points, and only when fewer than 3 hits already reach the definition score. It returns at most 5 rows.

Queries shorter than 2 code points, blank queries, and queries longer than 120 code points are rejected. A triliteral root is long enough to search.
