import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { canonicalSlug, partOfSpeechLabel, publicJson, type LookupHit } from "@/lib/dictionary";
import { siteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

type PublicRoot = {
  original: string;
  normalized: string;
  radicalCount: number;
  slug: string;
  entries: LookupHit[];
};

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug: rawSlug } = await params;
  const slug = canonicalSlug(rawSlug);
  const root = await publicJson<PublicRoot>(`/api/v1/public/dictionary/roots/${encodeURIComponent(slug)}`);
  if (!root) return { title: "جذر غير منشور", robots: { index: false, follow: false } };
  return {
    title: `${root.original} - الجذر`,
    description: `المداخل المنشورة المرتبطة بالجذر ${root.original}.`,
    alternates: { canonical: `${siteUrl}/root/${root.slug}` },
  };
}

export default async function RootPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug: rawSlug } = await params;
  const slug = canonicalSlug(rawSlug);
  const root = await publicJson<PublicRoot>(`/api/v1/public/dictionary/roots/${encodeURIComponent(slug)}`);
  if (!root) notFound();

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <h1 className="font-display text-6xl">{root.original}</h1>
      <p className="mt-3 text-muted">{root.radicalCount} أحرف</p>
      <section className="mt-8">
        <h2 className="font-display text-3xl">المداخل</h2>
        {root.entries.length === 0 ? (
          <p className="mt-4 text-muted">لا توجد مداخل منشورة مرتبطة بهذا الجذر.</p>
        ) : (
          <ul className="mt-4 space-y-3">
            {root.entries.map((entry) => (
              <li key={entry.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
                <Link href={`/word/${entry.slug}`} className="font-display text-3xl">
                  {entry.lemmaOriginal}
                </Link>
                <p className="text-sm text-muted">{partOfSpeechLabel(entry.partOfSpeech)}</p>
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
