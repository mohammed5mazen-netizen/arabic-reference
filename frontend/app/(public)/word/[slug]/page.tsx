import type { Metadata } from "next";
import Link from "next/link";
import { notFound, permanentRedirect } from "next/navigation";
import {
  attributionLine,
  canonicalSlug,
  partOfSpeechLabel,
  publicJson,
  relationLabel,
  searchPath,
  wordDescription,
  wordTitle,
  type PublicEntry,
} from "@/lib/dictionary";
import { featureLines, morphologyLabel, type EntryMorphology } from "@/lib/morphology";
import { publicMetadata } from "@/lib/metadata";
import { definedTermJsonLd, describe, jsonLd } from "@/lib/seo";
import { absoluteUrl } from "@/lib/site";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { ProvenanceBadge } from "@/components/ui/badge";
import { SourceCard } from "@/components/ui/source-card";
import { RelatedContent } from "@/components/ui/related-content";

export const dynamic = "force-dynamic";

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug: rawSlug } = await params;
  const slug = canonicalSlug(rawSlug);
  const entry = await publicJson<PublicEntry>(`/api/v1/public/dictionary/by-slug/${encodeURIComponent(slug)}`);
  if (!entry) {
    return publicMetadata({ title: "مدخل غير منشور", description: "هذا المدخل غير منشور.", path: "/search", index: false });
  }
  const meaning = entry.senses[0]?.shortDefinition || entry.senses[0]?.definition;
  return publicMetadata({
    title: wordTitle(entry.lemmaOriginal),
    description: wordDescription(meaning) ?? entry.lemmaOriginal,
    path: `/word/${entry.slug}`,
  });
}

export default async function WordPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug: rawSlug } = await params;
  const slug = canonicalSlug(rawSlug);
  const entry = await publicJson<PublicEntry>(`/api/v1/public/dictionary/by-slug/${encodeURIComponent(slug)}`);
  if (!entry) notFound();
  if (slug !== entry.slug) permanentRedirect(`/word/${entry.slug}`);
  const morphology = await publicJson<EntryMorphology>(`/api/v1/public/dictionary/entries/${entry.id}/morphology`);
  const lessons = await publicJson<Array<{ title: string; pathSlug: string; lessonSlug: string }>>(`/api/v1/public/learning/references?kind=DICTIONARY_ENTRY&slug=${encodeURIComponent(entry.slug)}`);
  const meaning = entry.senses[0]?.shortDefinition || entry.senses[0]?.definition || entry.lemmaOriginal;
  const readings = morphology?.readings ?? [];
  const plurals = morphology?.recordedPlurals ?? [];
  const showMorphology = readings.length > 0 || plurals.length > 0;

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={[{ label: "الرئيسية", href: "/" }, ...(entry.root ? [{ label: entry.root.original, href: `/root/${entry.root.slug}` }] : []), { label: entry.lemmaOriginal }]} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLd(definedTermJsonLd({ name: entry.lemmaOriginal, description: describe(meaning, entry.lemmaOriginal), url: absoluteUrl(`/word/${entry.slug}`) })) }} />
      <h1 className="font-display text-6xl">{entry.lemmaOriginal}</h1>
      <p className="mt-3 text-lg text-muted">
        {[entry.vocalizedForm, partOfSpeechLabel(entry.partOfSpeech), entry.root ? `الجذر ${entry.root.original}` : null]
          .filter(Boolean)
          .join(" · ")}
      </p>
      {entry.root ? (
        <p className="mt-3">
          <Link href={`/root/${entry.root.slug}`} className="text-library">
            مدخلات الجذر
          </Link>
        </p>
      ) : null}
      <section className="mt-10 space-y-6">
        <h2 className="font-display text-3xl">المعاني</h2>
        <ol className="space-y-4">
          {entry.senses.map((sense) => (
            <li key={sense.displayOrder} className="rounded-[1.5rem] border border-line bg-raised p-5">
              <p className="text-sm text-muted">{sense.displayOrder}</p>
              <p className="mt-2 text-lg leading-8">{sense.definition}</p>
              {sense.examples.length > 0 ? (
                <ul className="mt-4 space-y-2 text-muted">
                  {sense.examples.map((example) => (
                    <li key={example.textOriginal}>{example.textOriginal}</li>
                  ))}
                </ul>
              ) : null}
              {sense.sources.length > 0 ? (
                <ul className="mt-4 space-y-2">
                  {sense.sources.map((source) => (
                    <li key={attributionLine(source)}><SourceCard title={attributionLine(source)} /></li>
                  ))}
                </ul>
              ) : null}
            </li>
          ))}
        </ol>
      </section>
      {showMorphology ? (
        <section className="mt-10">
          <h2 className="font-display text-3xl">التحليل الصرفي</h2>
          <div className="mt-4 space-y-4">
            {readings.map((reading, index) => (
              <article key={`${reading.patternOriginal ?? "reading"}-${index}`} className="rounded-[1.5rem] border border-line bg-raised p-5">
                <p>{[reading.patternOriginal, morphologyLabel(reading.patternCategory), morphologyLabel(reading.derivation)].filter(Boolean).join(" · ")}</p>
                <p className="mt-2"><ProvenanceBadge kind="documented" /></p>
                {reading.verbClass ? <p className="mt-2 text-sm text-muted">الصنف: {morphologyLabel(reading.verbClass)}</p> : null}
                {reading.imperfectVowel ? <p className="text-sm text-muted">حركة العين في المضارع: {morphologyLabel(reading.imperfectVowel)}</p> : null}
                {reading.notes ? <p className="mt-2">{reading.notes}</p> : null}
                {featureLines(reading.features).length > 0 ? (
                  <ul className="mt-2 text-sm">
                    {featureLines(reading.features).map((line) => (
                      <li key={line}>{line}</li>
                    ))}
                  </ul>
                ) : null}
              </article>
            ))}
            {plurals.length > 0 ? (
              <p>جموع مسجّلة في المعجم: {plurals.join("، ")}</p>
            ) : null}
          </div>
        </section>
      ) : null}
      {entry.forms.length > 0 ? (
        <section className="mt-10">
          <h2 className="font-display text-3xl">الأشكال</h2>
          <ul className="mt-4 space-y-2">
            {entry.forms.map((form) => (
              <li key={`${form.formType}-${form.originalForm}`}>
                {form.originalForm} <bdi dir="ltr" className="text-muted">{form.formType}</bdi>
              </li>
            ))}
          </ul>
        </section>
      ) : null}
      {entry.relations.length > 0 ? (
        <section className="mt-10">
          <h2 className="font-display text-3xl">العلاقات</h2>
          <ul className="mt-4 space-y-2">
            {entry.relations.map((relation) => (
              <li key={`${relation.relationType}-${relation.otherSlug}`}>
                {relationLabel(relation.relationType)}: <Link href={`/word/${relation.otherSlug}`}>{relation.otherLemma}</Link>
              </li>
            ))}
          </ul>
        </section>
      ) : null}
      <RelatedContent
        links={[
          ...(entry.root ? [{ href: `/root/${entry.root.slug}`, label: `اقرأ جذر ${entry.root.original}` }] : []),
          { href: `/tools/morphology?word=${encodeURIComponent(entry.lemmaOriginal)}`, label: `حلّل ${entry.lemmaOriginal} صرفيًا` },
          { href: searchPath(entry.lemmaOriginal), label: `ابحث عن ${entry.lemmaOriginal}` },
          ...(lessons ?? []).map((lesson) => ({ href: `/learn/${lesson.pathSlug}/${lesson.lessonSlug}`, label: lesson.title })),
        ]}
      />
    </main>
  );
}
