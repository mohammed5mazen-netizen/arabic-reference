import type { Metadata } from "next";
import Link from "next/link";
import { SearchPanel } from "@/components/search-panel";
import { emptyLookupMessage, partOfSpeechLabel, publicJson, type LookupPage } from "@/lib/dictionary";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "البحث",
  robots: { index: false, follow: true },
};

export default async function SearchPage({ searchParams }: { searchParams: Promise<{ q?: string }> }) {
  const { q } = await searchParams;
  const query = q?.trim() ?? "";
  const page = query ? await publicJson<LookupPage>(`/api/v1/public/dictionary/lookup?word=${encodeURIComponent(query)}&page=0&size=20`) : null;

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <h1 className="font-display text-5xl">البحث</h1>
      <SearchPanel />
      {query ? (
        <section className="mt-8 space-y-4" aria-live="polite">
          <h2 className="text-lg text-muted">نتائج «{query}»</h2>
          {!page || page.items.length === 0 ? (
            <p className="rounded-[1.5rem] border border-line bg-raised p-6 text-lg">{emptyLookupMessage}</p>
          ) : (
            <ul className="space-y-3">
              {page.items.map((item) => (
                <li key={item.id} className="rounded-[1.5rem] border border-line bg-raised p-5">
                  <Link href={`/word/${item.slug}`} className="font-display text-3xl">
                    {item.lemmaOriginal}
                  </Link>
                  <p className="mt-2 text-sm text-muted">
                    {[item.vocalizedForm, partOfSpeechLabel(item.partOfSpeech), item.root ? `الجذر ${item.root}` : null]
                      .filter(Boolean)
                      .join(" · ")}
                  </p>
                  {item.shortDefinition ? <p className="mt-3 leading-8">{item.shortDefinition}</p> : null}
                  {item.senseCount > 1 ? <p className="mt-2 text-sm text-muted">{item.senseCount} معانٍ</p> : null}
                </li>
              ))}
            </ul>
          )}
        </section>
      ) : null}
    </main>
  );
}
