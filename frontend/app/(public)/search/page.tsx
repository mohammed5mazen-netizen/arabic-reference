import type { Metadata } from "next";
import Link from "next/link";
import { SearchPanel } from "@/components/search-panel";
import { publicJson } from "@/lib/dictionary";
import {
  dictionaryCard,
  emptySearchMessage,
  highlightSegments,
  matchReasonLabel,
  resultTypeLabel,
  searchFilterLayout,
  searchHref,
  type SearchHighlight,
  type SearchHit,
  type SearchPage,
} from "@/lib/search";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "البحث",
  robots: { index: false, follow: true },
};

export default async function SearchPage({ searchParams }: { searchParams: Promise<{ q?: string; type?: string; page?: string }> }) {
  const params = await searchParams;
  const query = params.q?.trim() ?? "";
  const type = params.type ?? "all";
  const page = Math.max(1, Number(params.page ?? "1") || 1);
  const results = query
    ? await publicJson<SearchPage>(`/api/v1/public/search?q=${encodeURIComponent(query)}&type=${encodeURIComponent(type)}&page=${page}&size=20`)
    : null;
  const facets = results?.facets;

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <h1 className="font-display text-4xl sm:text-5xl">{query ? `نتائج البحث عن «${query}»` : "البحث"}</h1>
      <SearchPanel initialQuery={query} />
      {query ? (
        <section className="mt-8 space-y-4" aria-live="polite">
          <nav aria-label="تصفية النتائج" className={`flex gap-2 overflow-x-auto pb-1 ${searchFilterLayout}`}>
            <Filter href={searchHref(query, "all")} current={type === "all"} label={`الكل${facets ? ` (${facets.dictionary + facets.roots + facets.grammar + (facets.content ?? 0)})` : ""}`} />
            <Filter href={searchHref(query, "dictionary")} current={type === "dictionary"} label={`المعجم${facets ? ` (${facets.dictionary})` : ""}`} />
            <Filter href={searchHref(query, "root")} current={type === "root"} label={`الجذور${facets ? ` (${facets.roots})` : ""}`} />
            <Filter href={searchHref(query, "grammar")} current={type === "grammar"} label={`النحو${facets ? ` (${facets.grammar})` : ""}`} />
            <Filter href={searchHref(query, "content")} current={type === "content"} label={`المحتوى${facets ? ` (${facets.content ?? 0})` : ""}`} />
          </nav>
          {!results || results.items.length === 0 ? (
            <div className="rounded-[1.5rem] border border-line bg-raised p-6">
              <p className="text-lg">{emptySearchMessage}</p>
              <p className="mt-3 text-sm text-muted">جرّب كلمة أقصر، أو أزل التشكيل، أو ابحث عن الجذر.</p>
            </div>
          ) : (
            <ul className="space-y-3">
              {results.items.map((item) => (
                <li key={`${item.type}-${item.id}`}>
                  <ResultCard hit={item} />
                </li>
              ))}
            </ul>
          )}
          {results && results.total > results.size ? (
            <nav aria-label="صفحات النتائج" className="flex gap-3">
              {page > 1 ? <Link href={searchHref(query, type, page - 1)} className="rounded-full border border-line px-4 py-2">السابق</Link> : null}
              {page * results.size < results.total ? <Link href={searchHref(query, type, page + 1)} className="rounded-full border border-line px-4 py-2">التالي</Link> : null}
            </nav>
          ) : null}
        </section>
      ) : null}
    </main>
  );
}

function Filter({ href, current, label }: { href: string; current: boolean; label: string }) {
  return (
    <Link href={href} aria-current={current ? "page" : undefined} className="shrink-0 rounded-full border border-line px-4 py-2">
      {label}
    </Link>
  );
}

function ResultCard({ hit }: { hit: SearchHit }) {
  const reason = matchReasonLabel(hit.matchReason);
  const card = dictionaryCard(hit);
  const titleRanges = hit.highlights.filter((range) => range.field === "title");
  const snippetRanges = hit.highlights.filter((range) => range.field === "snippet");
  return (
    <article className="rounded-[1.5rem] border border-line bg-raised p-5">
      <p className="text-sm text-muted">{resultTypeLabel(hit.type)}</p>
      <Link href={hit.url} className="mt-1 block font-display text-3xl">
        <Highlighted text={hit.type === "ROOT" ? `الجذر: ${hit.title}` : hit.title} ranges={titleRanges} />
      </Link>
      {hit.type === "DICTIONARY_ENTRY" ? (
        <p className="mt-2 text-sm text-muted">{[card.vocalized, card.speech, card.root].filter(Boolean).join(" · ")}</p>
      ) : null}
      {hit.type === "ROOT" && hit.metadata?.relatedCount ? <p className="mt-2 text-sm text-muted">{hit.metadata.relatedCount} مدخلًا منشورًا</p> : null}
      {hit.type === "ROOT" && hit.subtitle ? <p className="mt-2 leading-8">{hit.subtitle}</p> : null}
      {hit.snippet ? <p className="mt-3 leading-8"><Highlighted text={hit.snippet} ranges={snippetRanges} /></p> : null}
      {reason ? <p className="mt-3 text-sm text-muted">{reason}</p> : null}
    </article>
  );
}

function Highlighted({ text, ranges }: { text: string; ranges: SearchHighlight[] }) {
  return highlightSegments(text, ranges).map((part, index) =>
    part.highlighted ? <mark key={index}>{part.text}</mark> : <span key={index}>{part.text}</span>,
  );
}
