import type { Metadata } from "next";
import Link from "next/link";
import { notFound, permanentRedirect } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { canonicalSlug, partOfSpeechLabel, publicJson, type LookupHit } from "@/lib/dictionary";
import { publicMetadata } from "@/lib/metadata";
import { morphologyLabel, type RootMorphology } from "@/lib/morphology";
import { definedTermJsonLd, jsonLd } from "@/lib/seo";
import { absoluteUrl } from "@/lib/site";

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
  if (!root) return publicMetadata({ title: "جذر غير منشور", description: "هذا الجذر غير منشور.", path: "/search", index: false });
  return publicMetadata({
    title: `${root.original} — الجذر`,
    description: `المداخل المنشورة المرتبطة بالجذر ${root.original}.`,
    path: `/root/${root.slug}`,
  });
}

export default async function RootPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug: rawSlug } = await params;
  const slug = canonicalSlug(rawSlug);
  const root = await publicJson<PublicRoot>(`/api/v1/public/dictionary/roots/${encodeURIComponent(slug)}`);
  if (!root) notFound();
  if (slug !== root.slug) permanentRedirect(`/root/${root.slug}`);
  const morphology = await publicJson<RootMorphology>(`/api/v1/public/morphology/roots/${encodeURIComponent(root.slug)}`);
  const patterns = morphology?.patterns ?? [];

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={[{ label: "الرئيسية", href: "/" }, { label: root.original }]} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLd(definedTermJsonLd({ name: root.original, description: `المداخل المنشورة المرتبطة بالجذر ${root.original}.`, url: absoluteUrl(`/root/${root.slug}`) })) }} />
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
      {patterns.length > 0 ? (
        <section className="mt-10">
          <h2 className="font-display text-3xl">أوزان مسجّلة</h2>
          <ul className="mt-4 space-y-3">
            {patterns.map((pattern) => (
              <li key={`${pattern.entrySlug}-${pattern.code ?? pattern.original}`} className="rounded-[1.5rem] border border-line bg-raised p-4">
                <Link href={`/word/${pattern.entrySlug}`} className="font-display text-3xl">
                  {pattern.lemma}
                </Link>
                <p className="text-sm text-muted">{[pattern.original, morphologyLabel(pattern.derivation)].filter(Boolean).join(" · ")}</p>
              </li>
            ))}
          </ul>
        </section>
      ) : null}
    </main>
  );
}
