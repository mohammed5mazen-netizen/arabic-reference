# Public experience

S10 hardens the public site. It does not add a linguistic domain and it does not put a login wall in front of reading.

## Information architecture

The header groups existing public routes:

- اللغة: المعجم (search), النحو, الصرف, الإملاء, البلاغة
- المعرفة: الأدب, المقالات
- الخدمات: الأدوات, التعلّم, المساعد

Search stays in the header and on the homepage. The mobile menu is a native dialog with an explicit close control. Desktop groups use `details`.

There is no public dictionary index. المعجم opens `/search`. المصادر stays unpublished until that section exists, so the footer does not link to it. S12 does not add a public sources route for the sitemap. Canonicals, breadcrumbs, and the sitemap follow [PUBLIC_URL_POLICY.md](PUBLIC_URL_POLICY.md). The manual visual checklist stays open until S13.

## Homepage

The homepage answers what the site is, offers search, lists the language areas, four real tools, published learning paths when they exist, and a secondary assistant entry. If no path is published, the learning block is a short explanation and a link to `/learn`. It does not invent path cards.

## Reading pages

Word, root, grammar, spelling, rhetoric, literature, articles, tools, assistant, and learning pages keep their published data. S10 adds a shared page header, source cards, provenance badges, related links that come from the record, and a reading width. Morphology still names a possible analysis in text, not by color alone.

## States

- Empty search: لم نعثر على نتائج مطابقة, plus a shorter-query hint and a link to tools.
- Missing page: الصفحة غير موجودة, with home, search, and section links.
- Failed request: تعذر فتح الصفحة, with إعادة المحاولة. No stack trace.
- Assistant off: a calm note plus search and tools. The homepage does not turn into an error.

Quiz answers stay off the page until submit. Admin routes stay out of the public header and footer.
