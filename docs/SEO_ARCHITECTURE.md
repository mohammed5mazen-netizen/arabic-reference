# SEO architecture

S12 makes published knowledge discoverable. It does not add keyword pages, ads, or analytics.

`SITE_URL` is the canonical origin. `NEXT_PUBLIC_SITE_URL` is used only when `SITE_URL` is empty. `resolveSiteUrl()` normalizes that origin: absolute `http` or `https`, no query, no fragment, no path, trailing slash removed. Development may use `http://localhost:3000` when neither variable is set. A production server (`next start`) throws if the origin is missing, so it cannot emit a localhost canonical by accident. Indexing is a separate switch, `SEO_INDEXING_ENABLED`, and it is off unless the value is exactly `true`.

Public pages build titles, descriptions, canonicals, and Open Graph through `publicMetadata`. Descriptions come from the published summary or definition and are cut at 160 characters. They are not written by a model.

The sitemap is generated from two sources. Static hubs and tool landing routes live in `corePublicPaths`. Published record URLs come from `GET /api/v1/public/discovery`, which reads `search_document` in pages of at most 500. The frontend splits those pages into sitemap chunks of 5,000 URLs, under the usual 50,000 limit, and emits a sitemap index. `lastModified` is the stored `published_at`. The request time is not used. Redis and the assistant are not on this path.

`/admin/seo` and `GET /api/v1/admin/seo/status` report the configured origin, indexing flag, counts, orphan dictionary entries, and broken relations. The permission is `seo.admin.view` for the owner, admin, and auditor. Noindex is not the protection for admin routes. Authentication is.
