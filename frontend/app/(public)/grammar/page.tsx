import type { Metadata } from "next";
import Link from "next/link";
import { grammarLabel, type GrammarCrumb } from "@/lib/grammar";
import { publicJson } from "@/lib/dictionary";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

type LinkItem = { title: string; slug: string; summary?: string | null; difficultyLabel?: string | null };
type Index = {
  title: string;
  introduction: string;
  categories: { code: string; label: string; count: number }[];
  topics: { items: LinkItem[]; total: number };
};
type SearchPage = { items: { kind: string; title: string; slug: string; summary?: string | null }[]; total: number };

export const metadata: Metadata = {
  title: "النحو",
  description: "مرجع منظّم في النحو العربي: موضوعات، قواعد، ومصطلحات موثّقة.",
  alternates: { canonical: `${resolveSiteUrl()}/grammar` },
};

export default async function GrammarHomePage({ searchParams }: { searchParams: Promise<{ q?: string }> }) {
  const { q } = await searchParams;
  const query = q?.trim() ?? "";
  const index = await publicJson<Index>("/api/v1/public/grammar/topics?page=0&size=50");
  const results = query ? await publicJson<SearchPage>(`/api/v1/public/grammar/search?q=${encodeURIComponent(query)}&page=0&size=20`) : null;
  const crumbs: GrammarCrumb[] = [{ label: "الرئيسية", href: "/" }, { label: "النحو" }];

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={crumbs} />
      <h1 className="font-display text-5xl sm:text-6xl">{index?.title ?? "النحو"}</h1>
      <p className="mt-4 leading-8 text-muted">{index?.introduction}</p>
      <form action="/grammar" className="mt-8 flex flex-col gap-3 sm:flex-row">
        <label className="sr-only" htmlFor="grammar-search">ابحث في النحو</label>
        <input id="grammar-search" name="q" defaultValue={query} placeholder="ابحث في الموضوعات والقواعد والمصطلحات" className="min-h-14 flex-1 rounded-2xl border border-line bg-raised px-4 text-lg" />
        <button type="submit" className="min-h-14 rounded-2xl bg-library px-6 text-white">ابحث</button>
      </form>
      {query ? (
        <section className="mt-8 space-y-3" aria-live="polite">
          <h2 className="font-display text-3xl">نتائج «{query}»</h2>
          {results?.items.length ? results.items.map((item) => (
            <article key={`${item.kind}-${item.slug}`} className="rounded-[1.5rem] border border-line bg-raised p-5">
              <p className="text-sm text-muted">{grammarLabel(item.kind)}</p>
              <Link href={item.kind === "RULE" ? `/grammar/rules/${item.slug}` : item.kind === "CONCEPT" ? `/grammar/concepts/${item.slug}` : `/grammar/${item.slug}`} className="font-display text-3xl">{item.title}</Link>
              {item.summary ? <p className="mt-2 leading-8">{item.summary}</p> : null}
            </article>
          )) : <p>لا توجد نتائج منشورة لهذا البحث.</p>}
        </section>
      ) : null}
      <section className="mt-10" aria-labelledby="grammar-categories">
        <h2 id="grammar-categories" className="font-display text-3xl">التصنيفات</h2>
        <ul className="mt-4 grid gap-3 sm:grid-cols-2">
          {index?.categories.map((category) => (
            <li key={category.code} className="rounded-[1.5rem] border border-line bg-raised p-4">
              <p className="font-display text-2xl">{category.label}</p>
              <p className="text-sm text-muted">{category.count} موضوعًا منشورًا</p>
            </li>
          ))}
        </ul>
      </section>
      <section className="mt-10 space-y-3" aria-labelledby="grammar-topics">
        <h2 id="grammar-topics" className="font-display text-3xl">الموضوعات</h2>
        {index?.topics.items.length ? index.topics.items.map((topic) => (
          <article key={topic.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
            <Link href={`/grammar/${topic.slug}`} className="font-display text-3xl">{topic.title}</Link>
            {topic.difficultyLabel ? <p className="mt-1 text-sm text-muted">{topic.difficultyLabel}</p> : null}
            {topic.summary ? <p className="mt-2 leading-8">{topic.summary}</p> : null}
          </article>
        )) : <p>لا توجد موضوعات منشورة بعد.</p>}
      </section>
    </main>
  );
}
