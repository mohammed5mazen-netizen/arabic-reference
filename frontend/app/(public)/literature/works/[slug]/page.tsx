import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { canonicalSlug, publicJson } from "@/lib/dictionary";
import { excerptBlockedMessage, knowledgeCrumbs, poetryClass, rightsAllowExcerpt, rightsLabel, showsFullTextButton } from "@/lib/knowledge";
import { resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";
type Work = {
  title: string;
  description?: string | null;
  rights?: string | null;
  rightsNote?: string | null;
  attribution?: string | null;
  compositionDisplay?: string | null;
  aliases: string[];
  figures: { name?: string; slug?: string }[];
  excerpts: { text?: string }[];
  sources: { title?: string }[];
  genre?: { name?: string };
  era?: { name?: string };
};

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const work = await publicJson<Work>(`/api/v1/public/literature/works/${encodeURIComponent(canonicalSlug(slug))}`);
  const title = work?.title ?? "عمل أدبي";
  return { title, description: work?.description || title, alternates: { canonical: `${resolveSiteUrl()}/literature/works/${slug}` }, openGraph: { title, description: work?.description || title, url: `${resolveSiteUrl()}/literature/works/${slug}` } };
}

export default async function WorkPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const work = await publicJson<Work>(`/api/v1/public/literature/works/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!work) notFound();
  const excerpts = rightsAllowExcerpt(work.rights) ? work.excerpts : [];
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("الأدب", "/literature", [{ label: work.title }])} />
      <article>
        <p className="text-sm text-muted">{rightsLabel(work.rights)}</p>
        <h1 className="font-display text-5xl">{work.title}</h1>
        {work.genre?.name || work.era?.name ? <p className="mt-3 text-muted">{[work.genre?.name, work.era?.name].filter(Boolean).join(" · ")}</p> : null}
        {work.description ? <p className="mt-4 leading-8">{work.description}</p> : null}
        {work.attribution ? <p className="mt-3"><cite>{work.attribution}</cite></p> : null}
        {excerpts.length ? excerpts.map((excerpt, index) => <blockquote key={index} className={`mt-6 ${poetryClass}`}>{excerpt.text}</blockquote>) : <p className="mt-6">{excerptBlockedMessage}</p>}
        {showsFullTextButton() ? <span>قراءة الكتاب</span> : null}
        {work.sources.length ? <ul className="mt-8">{work.sources.map((source, index) => <li key={index}><cite>{source.title}</cite></li>)}</ul> : null}
      </article>
    </main>
  );
}
