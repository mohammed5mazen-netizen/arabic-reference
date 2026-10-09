# Sitemap policy

`/sitemap.xml` is a sitemap index. `/sitemap/core.xml` lists the homepage, section hubs, tool landings, `/learn`, and `/assistant`. `/sitemap/knowledge-0.xml` and later chunks list published paths from `search_document`.

Included when published and present in the search index: dictionary words, roots, grammar topics, rules, and concepts, spelling topics and rules, rhetoric topics and devices, literary eras, figures, and works, articles, learning paths, and lessons.

Excluded: `/admin/**`, `/api/**`, drafts, `IN_REVIEW`, verified-but-unpublished rows, archived rows, `/search`, tool URLs with a query, assistant question state, quiz attempts, and error pages. `/sources` is not a public route, so it is not listed. Genres and schools are not separate public pages.

A path is dropped if it contains `?`, `#`, `%`, or starts with `/admin`, `/api`, or `/search`. The response has a type, a path, and `publishedAt`. It does not include draft titles or internal ids.

The query is paged. One sitemap chunk holds at most 5,000 URLs, and the index stops at 10 chunks until a later split is needed. Generation does not load the whole table into memory.

Publishing writes the search document, so the next sitemap request includes the URL. Archiving deletes the search document, so the next request omits it. The sitemap route is dynamic, so it does not wait on a long cache.
