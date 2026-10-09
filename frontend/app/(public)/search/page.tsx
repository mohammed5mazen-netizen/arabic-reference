import type { Metadata } from "next";
import Link from "next/link";
import { SearchPanel } from "@/components/search-panel";
import { EmptyState } from "@/components/ui/empty-state";
import { ProvenanceBadge } from "@/components/ui/badge";
import { LinkButton } from "@/components/ui/button";
import { formatNumber } from "@/lib/format";
import { apiBase } from "@/lib/dictionary";
import { publicMetadata } from "@/lib/metadata";
import {
  dictionaryCard,
  emptySearchMessage,
  searchPresentation,
  unavailableSearchMessage,
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

export async function generateMetadata(): Promise<Metadata> {
  return publicMetadata({
    title: "البحث",
    description: "بحث في المعرفة المنشورة. نتائج البحث لا تُفهرس.",
    path: "/search",
    index: false,
  });
}

export default async function SearchPage({ searchParams }: { searchParams: Promise<{ q?: string; type?: string; page?: string }> }) {
  const params = await searchParams;
  const query = params.q?.trim() ?? "";
  const type = params.type ?? "all";
  const page = Math.max(1, Number(params.page ?? "1") || 1);
  let failed = false;
  let results: SearchPage | null = null;
  if (query) {
    try {
      const response = await fetch(
        `${apiBase()}/api/v1/public/search?q=${encodeURIComponent(query)}&type=${encodeURIComponent(type)}&page=${page}&size=20`,
        { cache: "no-store" },
      );
      if (!response.ok) {
        failed = true;
      } else {
        const body = (await response.json()) as { data?: SearchPage };
        results = body.data ?? null;
        if (!results) failed = true;
      }
    } catch {
      failed = true;
    }
  }
  const presentation = query ? searchPresentation(results, failed) : null;
  const facets = results?.facets;

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <h1 className="font-display text-4xl sm:text-5xl">{query ? `نتائج البحث عن «${query}»` : "البحث"}</h1>
      <SearchPanel initialQuery={query} />
      {query ? (
        <section className="mt-8 space-y-4" aria-live="polite">
          <nav aria-label="تصفية النتائج" className={`flex gap-2 overflow-x-auto pb-1 ${searchFilterLayout}`}>
            <Filter href={searchHref(query, "all")} current={type === "all"} label={`الكل${facets ? ` (${formatNumber(facets.dictionary + facets.roots + facets.grammar + (facets.content ?? 0))})` : ""}`} />
            <Filter href={searchHref(query, "dictionary")} current={type === "dictionary"} label={`المعجم${facets ? ` (${formatNumber(facets.dictionary)})` : ""}`} />
            <Filter href={searchHref(query, "root")} current={type === "root"} label={`الجذور${facets ? ` (${formatNumber(facets.roots)})` : ""}`} />
            <Filter href={searchHref(query, "grammar")} current={type === "grammar"} label={`النحو${facets ? ` (${formatNumber(facets.grammar)})` : ""}`} />
            <Filter href={searchHref(query, "content")} current={type === "content"} label={`المحتوى${facets ? ` (${formatNumber(facets.content ?? 0)})` : ""}`} />
            <Filter href={searchHref(query, "learning")} current={type === "learning"} label="التعلّم" />
          </nav>
          {presentation === "unavailable" ? (
            <EmptyState
              title={unavailableSearchMessage}
              description="الخدمة غير متاحة مؤقتًا. أعد المحاولة بعد لحظات."
              action={<LinkButton href={searchHref(query, type, page)}>إعادة المحاولة</LinkButton>}
            />
          ) : !results || results.items.length === 0 ? (
            <EmptyState
              title={emptySearchMessage}
              description="جرّب صيغة أقصر، أو أزل التشكيل، أو افتح أداة ذات صلة."
              action={<LinkButton href="/tools" variant="secondary">الأدوات اللغوية</LinkButton>}
            />
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
    <Link href={href} aria-current={current ? "page" : undefined} className="inline-flex min-h-11 shrink-0 items-center rounded-full border border-line px-4">
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
      {hit.matchReason === "FUZZY" ? <p className="mt-3"><ProvenanceBadge kind="near" /></p> : null}
      {reason && hit.matchReason !== "FUZZY" ? <p className="mt-3 text-sm text-muted">{reason}</p> : null}
    </article>
  );
}

function Highlighted({ text, ranges }: { text: string; ranges: SearchHighlight[] }) {
  return highlightSegments(text, ranges).map((part, index) =>
    part.highlighted ? <mark key={index}>{part.text}</mark> : <span key={index}>{part.text}</span>,
  );
}
