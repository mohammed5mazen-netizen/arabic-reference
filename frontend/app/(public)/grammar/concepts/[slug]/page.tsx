import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { attributionLine, publicJson } from "@/lib/dictionary";
import { grammarCrumbs, grammarSlug, grammarTitle } from "@/lib/grammar";
import { publicMetadata } from "@/lib/metadata";
import { definedTermJsonLd, describe, jsonLd } from "@/lib/seo";
import { absoluteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

type Concept = {
  term: string;
  slug: string;
  shortDefinition?: string | null;
  detailedDefinition?: string | null;
  aliases: string[];
  rules: { title: string; slug: string }[];
  sources: { title?: string | null; author?: string | null; edition?: string | null; publicationYear?: number | null; pageFrom?: number | null; pageTo?: number | null }[];
};

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const concept = await load(params);
  if (!concept) return publicMetadata({ title: "مصطلح غير منشور", description: "هذا المصطلح غير منشور.", path: "/grammar", index: false });
  return publicMetadata({
    title: grammarTitle("concept", concept.term),
    description: concept.shortDefinition ?? concept.term,
    path: `/grammar/concepts/${concept.slug}`,
  });
}

export default async function GrammarConceptPage({ params }: { params: Promise<{ slug: string }> }) {
  const concept = await load(params);
  if (!concept) notFound();
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={grammarCrumbs([{ label: concept.term }])} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLd(definedTermJsonLd({ name: concept.term, description: describe(concept.shortDefinition, concept.term), url: absoluteUrl(`/grammar/concepts/${concept.slug}`) })) }} />
      <h1 className="font-display text-5xl">{concept.term}</h1>
      {concept.shortDefinition ? <p className="mt-4 text-lg leading-8">{concept.shortDefinition}</p> : null}
      {concept.detailedDefinition ? <p className="mt-4 leading-8">{concept.detailedDefinition}</p> : null}
      {concept.aliases.length > 0 ? (
        <section className="mt-8">
          <h2 className="font-display text-3xl">أسماء أخرى</h2>
          <ul className="mt-3 space-y-2">{concept.aliases.map((alias) => <li key={alias}>{alias}</li>)}</ul>
        </section>
      ) : null}
      {concept.rules.length > 0 ? (
        <section className="mt-8">
          <h2 className="font-display text-3xl">قواعد مرتبطة</h2>
          <ul className="mt-3 space-y-2">{concept.rules.map((rule) => <li key={rule.slug}><Link href={`/grammar/rules/${rule.slug}`}>{rule.title}</Link></li>)}</ul>
        </section>
      ) : null}
      {concept.sources.length > 0 ? (
        <section className="mt-8">
          <h2 className="font-display text-3xl">المصادر</h2>
          <ul className="mt-3 space-y-2">{concept.sources.map((source) => <li key={attributionLine(source)}>{attributionLine(source)}</li>)}</ul>
        </section>
      ) : null}
    </main>
  );
}

async function load(params: Promise<{ slug: string }>): Promise<Concept | null> {
  const { slug } = await params;
  return publicJson<Concept>(`/api/v1/public/grammar/concepts/${encodeURIComponent(grammarSlug(slug))}`);
}
