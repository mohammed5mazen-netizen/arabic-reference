# Public URL policy

The canonical URL is `SITE_URL` plus the stored slug path. Tracking parameters and search queries are not part of it. Each path segment is decoded at most twice and encoded once, so an Arabic slug is not written as `%25`.

If the requested slug decodes to a different string than the stored slug, the word, root, and learning pages respond with a permanent redirect to the stored slug. The app router collapses duplicate slashes. There is no redirect from a draft slug.

## Archive

An archived or unknown public record returns 404 through `notFound()`. The public API already hides rows without a published snapshot and rows whose status is archived. 410 was not added: the current routers have one not-found path, and a second status would not tell a crawler about a replacement that does not exist.

## Slug history

Slug history is deferred. `ContentSlugs.of` runs when a record is created and appends the first eight hex characters of the id. Later edits of the title do not call `setSlug` again, so a published URL stays put. Sitemap output therefore contains only the current path. If a future change rewrites a published slug, the old path must be stored and answered with 308 to the new canonical. Draft slugs must not redirect.

## Lists

`/search` is entirely `noindex`, including later pages. Section hubs are a single indexable URL. A hub query such as `/grammar?q=` is `noindex`, and its canonical is the hub without the query. Article, spelling, rhetoric, and literature indexes are one page each, so they do not canonicalise a later page back to page one.
