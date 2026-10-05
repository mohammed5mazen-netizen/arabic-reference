# Linguistic tools

S7 adds a public center at `/tools`. Each tool reads published knowledge through existing application ports. Nothing in this stage generates a meaning, a root, or a correction that the published data and the S3 rules do not already support.

## Tools

| Tool | Route | API |
| --- | --- | --- |
| المحلل الصرفي | `/tools/morphology` | `/api/v1/public/morphology/analyze` |
| مستكشف الجذر | `/tools/root` | `/api/v1/public/tools/root` |
| مستكشف المشتقات | `/tools/derivations` | `/api/v1/public/tools/derivations` |
| مستكشف الأوزان | `/tools/patterns` | `/api/v1/public/tools/patterns` |
| محلل الكلمة | `/tools/word-analysis` | `/api/v1/public/tools/word-analysis` |
| مقارنة الكلمات | `/tools/compare` | `/api/v1/public/tools/compare` |
| المرادفات والأضداد | `/tools/relations` | `/api/v1/public/tools/relations` |
| التحقق الإملائي المرجعي | `/tools/spelling-check` | `/api/v1/public/tools/spelling-check` |
| مستكشف القواعد النحوية | `/tools/grammar` | `/api/v1/public/tools/grammar` |
| مستكشف العلاقات اللغوية | `/tools/explore` | `/api/v1/public/tools/explore` |

The spelling tool is a reference check against dictionary forms and recorded mistakes. It is not an automatic corrector. The grammar tool searches published topics, rules, and concepts. It does not parse a sentence.

## Boundaries

`tools` may call dictionary, morphology, grammar, spelling, and article application services, plus `LinguisticSearchPort`. It does not call their infrastructure packages or their controllers. Those modules do not depend on `tools`. The morphology engine stays in `morphology`.

## Response

Each tool returns `input`, `normalizedInput`, `tool`, `status`, `result`, `provenance`, and `limitations`. Missing knowledge is reported as unavailable. A rule-derived root is labeled جذر محتمل. Synonyms and antonyms stay on the sense that recorded them. Several lexical entries for one word stay in separate cards.

## Cache, limits, and privacy

One Redis cache key combines the tool, the normalized input, the dictionary content stamp, the morphology rule generation, and the search index version. Publication, archive, and rule changes move that key. Results are not stored in PostgreSQL.

Anonymous calls share a per-minute ceiling (`app.tools.rate-limit-per-minute`, default 90). There is no login wall. Metrics count calls, empty results, errors, and duration by tool code. Raw input is not logged, and the platform keeps no visitor tool history.

`/admin/tools` shows the catalog and those aggregates to a principal with `tools.view`. Caps and the cache TTL are server configuration.

## Graph

The relation explorer keeps depth at most 2 and nodes at most 50. The same bounds apply to derivation and candidate lists. The page always renders the nodes as a list. A compact desktop view is additional.
