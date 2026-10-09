# Manual UI checklist

Visual launch gate, 9 October 2026. Chrome headless via Playwright against the production frontend on `http://127.0.0.1:3013` and the API on port 8080. AI disabled. Indexing off. Origin `https://reference.example`.

Statuses mean what was seen in that pass. `NOT TESTED` is not a pass.

## Desktop, 1440 and 1024

- PASS — Homepage in light and dark: title, search, language sections, tools, learning empty of fake cards, assistant as a secondary block, header and footer
- PASS — Search results for تحرير, normalized تحريـر, and the empty query `zzzz-no-result`
- NOT TESTED — Search pagination. The published index has two dictionary rows, so a second page never appears
- PASS — Published word تحرير: lemma, sense, source, related links
- NOT TESTED — Published root, grammar rule, spelling, rhetoric, literature, and article detail. Those catalogs are empty in this database
- PASS — Grammar, spelling, rhetoric, literature, and articles hubs render their empty catalogs without overflow
- PASS — Tools hub and `/tools/root?q=كتب`
- PASS — Assistant disabled state
- PASS — Learning hub empty state
- NOT TESTED — A published path, lesson, or quiz
- PASS — Admin desk, editorial dashboard, review inbox, publishing inbox, quality, sources, and SEO at 1440 and 1024
- PASS — Editorial record: readiness, timeline, comments, and diff at 1440

## Phone, 320, 375, and tablet 768

- PASS — Public pages in the sweep, including homepage, search, word, grammar, tools, assistant, and learn: no page-level horizontal overflow
- PASS — Mobile menu opens inside the viewport, Escape closes it, and the close button closes it
- PASS — Search field and button stack, and Arabic wraps
- PASS — Admin editorial, quality, and sources: the page itself does not scroll sideways. Wide tables scroll inside their own container
- PASS — Editorial diff stacks on a 375px viewport
- NOT TESTED — Quiz tap targets and lesson contents. No published lesson or quiz

## Light, dark, and persistence

- PASS — Light homepage, word, tools, and admin surfaces are readable
- PASS — Dark homepage and assistant are readable
- PASS — Choosing dark, then reloading, stays dark. Choosing light, then reloading, stays light
- PASS — The theme script sets the class before paint. No wrong-theme flash was observed on reload

## Keyboard and focus

- PASS — First Tab lands on «تجاوز إلى المحتوى» with a visible outline. The next Tab lands on «المرجع العربي» with the same outline
- PASS — Mobile navigation dialog closes with Escape
- NOT TESTED — A full Tab walk of filters, tool forms, quiz choices, and every admin action

## Arabic and mixed direction

- PASS — Arabic paragraphs, the word page, search snippets, and the admin diff stay right to left
- PASS — Latin identifiers in the diff and source metadata sit inside the Arabic layout without reversing the sentence
- NOT TESTED — A morphology form-code line on a published root. No published root

## Flows

- PASS — Anonymous reading has no signup wall
- NOT TESTED — Quiz answer marker before submit. No published quiz
- PASS — Assistant disabled state points to search and tools and is not an error page
- PASS — Unknown route is Arabic, «الصفحة غير موجودة», with a way back
- PASS — Unknown word says «لم نعثر على مدخل منشور لهذه الكلمة» and keeps the header and footer
- PASS — With the API stopped, the homepage stays a readable shell, search says «تعذر إتمام البحث الآن» and offers «إعادة المحاولة», and the assistant stays on its disabled state. No stack trace

## Accessibility

- NOT TESTED — axe scan. axe is not installed in this workspace
- PASS — Computed contrast for muted text and primary buttons in light and dark is above WCAG AA 4.5:1
