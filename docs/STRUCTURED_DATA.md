# Structured data

JSON-LD is emitted only for a type the page actually is. Values are passed through `JSON.stringify` and `<`, `>`, and `&` are escaped so a title cannot close the script tag.

The homepage emits `WebSite` with `name`, `url`, and `inLanguage: ar`. Organization is deferred. The repository does not yet hold a legal name, address, or official identifier, and those are not invented.

Detail pages that show breadcrumbs emit `BreadcrumbList` from the same items as the visible nav. The last crumb is the current page and has no URL, matching the visible text.

A published article emits `Article` with `headline`, `description`, `datePublished` from the search document when that timestamp exists, and `dateModified` from the article `updated_at`. `author` is included only when the published editor name is non-empty.

A dictionary word, a root, and a grammar concept emit `DefinedTerm` with the published name, the published definition or a short factual description, the canonical URL, and `inLanguage: ar`.

A learning path is not a `Course`. It has no provider, no course instance, and no credential. The path and lesson pages use the visible page plus `BreadcrumbList` only.

Structured data is rendered with the published snapshot. A missing or archived record calls `notFound()` and does not emit a term or article graph.
