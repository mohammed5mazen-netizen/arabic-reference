import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { canonicalSlug, publicJson } from "@/lib/dictionary";
import { knowledgeCrumbs } from "@/lib/knowledge";
import { siteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";
type Era = { name: string; slug: string; startDescription?: string | null; endDescription?: string | null; summary?: string | null; historicalContext?: string | null; sources: { title?: string }[] };

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const era = await publicJson<Era>(`/api/v1/public/literature/eras/${encodeURIComponent(canonicalSlug(slug))}`);
  const title = era?.name ?? "حقبة أدبية";
  return { title, description: era?.summary || title, alternates: { canonical: `${siteUrl}/literature/eras/${slug}` }, openGraph: { title, description: era?.summary || title, url: `${siteUrl}/literature/eras/${slug}` } };
}

export default async function EraPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const era = await publicJson<Era>(`/api/v1/public/literature/eras/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!era) notFound();
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("الأدب", "/literature", [{ label: era.name }])} />
      <article>
        <h1 className="font-display text-5xl">{era.name}</h1>
        <p className="mt-4 text-muted">{[era.startDescription, era.endDescription].filter(Boolean).join(" — ")}</p>
        {era.summary ? <p className="mt-4 leading-8">{era.summary}</p> : null}
        {era.historicalContext ? <p className="mt-4 leading-8">{era.historicalContext}</p> : null}
        {era.sources.length ? <ul className="mt-8">{era.sources.map((source, index) => <li key={index}><cite>{source.title}</cite></li>)}</ul> : null}
      </article>
    </main>
  );
}
