# Indexing policy

Indexing is enabled only when `SEO_INDEXING_ENABLED=true` and `SITE_URL` is an `https` origin that is not localhost, `127.0.0.1`, `::1`, or a `.local` host. Development, preview, and staging stay out of the index because the flag defaults to false. A preview deployment must leave the flag unset.

When indexing is off, `robots.txt` disallows `/` and every public page is `noindex`. When it is on, robots allows `/` and disallows `/admin/`, `/api/`, and `/search`. The sitemap URL is always the configured origin plus `/sitemap.xml`.

Indexable routes are the homepage, the section hubs (`/grammar`, `/spelling`, `/rhetoric`, `/literature`, `/articles`, `/tools`, `/learn`, `/assistant`), tool landing routes, and published knowledge pages. A hub may be empty and still return 200, because it explains the section. It does not invent detail pages.

These stay `noindex`: `/search` and any search query, a tool page with a query string, `/assistant` when `q` is present, admin, login, and unavailable records. Quiz attempts are not URLs. The lesson page stays indexable; the attempt token is a POST body and is absent from the sitemap.

`GOOGLE_SITE_VERIFICATION` renders a verification meta tag only when it is set. There is no Search Console client and no analytics cookie.

There is no hreflang. The document language is `ar`.
