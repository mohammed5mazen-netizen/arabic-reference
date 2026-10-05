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

## Canonical strategy

Each future knowledge URL has one canonical absolute URL based on `NEXT_PUBLIC_SITE_URL`. Variants that differ only by diacritics or tracking parameters canonicalize to the stable slug. The normalized text is a search key, not a second public URL, unless an explicit alias redirect is added later.

## Sitemap growth

When words, roots, meanings, grammar articles, and terms exist, sitemaps should be partitioned (for example by type and page) and referenced from a sitemap index. Generation belongs to the data pipeline of S12, not to a static file of fictional URLs.

## Rendering

Knowledge pages stay server-rendered so the primary text is in the first HTML response. Client components are limited to interaction such as theme and the search field.
