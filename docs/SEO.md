# SEO

Public knowledge pages must be reachable by crawlers. Authentication middleware must not sit in front of them.

## S0 foundation

- Arabic `lang` and `dir="rtl"` on the document
- title, description, canonical URL, Open Graph, and `robots` index/follow
- `app/robots.ts` allows `/`
- `app/sitemap.ts` lists the homepage only
- semantic landmarks: header, main, sections, footer
- one `h1`

No large generated sitemap is produced. `/search?q=` is the unified published search and is marked `noindex`. Word, root, and grammar pages stay server-rendered from published records. A dynamic sitemap remains S12 work.

Grammar pages at `/grammar`, `/grammar/{slug}`, `/grammar/rules/{slug}`, and `/grammar/concepts/{slug}` are indexable when the record is published. Each sets a specific title, such as `الفاعل - القاعدة والأمثلة`, a canonical URL, and a breadcrumb list taken from published ancestors. The sitemap remains the homepage until S12.

S6 adds the same treatment for `/spelling`, `/spelling/{slug}`, `/spelling/rules/{slug}`, `/rhetoric`, `/rhetoric/{slug}`, `/rhetoric/devices/{slug}`, `/literature`, `/literature/eras/{slug}`, `/literature/figures/{slug}`, `/literature/works/{slug}`, `/articles`, and `/articles/{slug}`. Each published page sets a title, a description, a canonical URL, and Open Graph fields. `/search` stays `noindex`. A dynamic sitemap of these URLs remains S12 work.

Tool landing pages (`/tools` and each `/tools/...` page without a query) are indexable. A shared result URL such as `/tools/root?q=كتاب` or `/tools/compare?a=كتاب&b=كاتب` canonicalizes to the tool page itself and is `noindex`. That keeps one public page per tool instead of a page per query. The absolute origin comes from `SITE_URL`, then `NEXT_PUBLIC_SITE_URL`, and only then `http://localhost:3000`. `SITE_URL` is read at request time so a deployed host does not keep a localhost canonical that was baked in at build time.

`/assistant` is one indexable landing page. A question is not placed in the URL as a document, and `/assistant?q=` is only a prefilled question. The sitemap remains the homepage until S12.

`/learn`, `/learn/{pathSlug}`, and `/learn/{pathSlug}/{lessonSlug}` are indexable when the path is published. Each sets a title, a description, a canonical URL, and breadcrumbs. A quiz attempt is not a URL, so there is no token page for a crawler to index. A dynamic sitemap of learning URLs remains S12 work.

## Canonical strategy

Each future knowledge URL has one canonical absolute URL based on `NEXT_PUBLIC_SITE_URL`. Variants that differ only by diacritics or tracking parameters canonicalize to the stable slug. The normalized text is a search key, not a second public URL, unless an explicit alias redirect is added later.

## Sitemap growth

When words, roots, meanings, grammar articles, and terms exist, sitemaps should be partitioned (for example by type and page) and referenced from a sitemap index. Generation belongs to the data pipeline of S12, not to a static file of fictional URLs.

## Rendering

Knowledge pages stay server-rendered so the primary text is in the first HTML response. Client components are limited to interaction such as theme and the search field.
