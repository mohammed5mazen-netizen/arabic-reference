import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { canonicalSlug, publicJson } from "@/lib/dictionary";
import { knowledgeCrumbs, spellingRulePath } from "@/lib/knowledge";
import { siteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

type Source = { title?: string; author?: string; attribution?: string };
type Topic = { title: string; slug: string; summary?: string | null; rules: { title: string; slug: string; summary?: string | null }[]; sources: Source[] };

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const topic = await publicJson<Topic>(`/api/v1/public/spelling/topics/${encodeURIComponent(canonicalSlug(slug))}`);
  const title = topic?.title ?? "موضوع إملائي";
  return {
    title,
    description: topic?.summary || title,
    alternates: { canonical: `${siteUrl}/spelling/${slug}` },
    openGraph: { title: `${title} | المرجع العربي`, description: topic?.summary || title, url: `${siteUrl}/spelling/${slug}` },
  };
}

export default async function SpellingTopicPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const topic = await publicJson<Topic>(`/api/v1/public/spelling/topics/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!topic) notFound();
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("الإملاء", "/spelling", [{ label: topic.title }])} />
      <h1 className="font-display text-5xl">{topic.title}</h1>
      {topic.summary ? <p className="mt-4 leading-8">{topic.summary}</p> : null}
      <section className="mt-8 space-y-3" aria-labelledby="topic-rules">
        <h2 id="topic-rules" className="font-display text-3xl">القواعد</h2>
        {topic.rules.length ? topic.rules.map((rule) => (
          <article key={rule.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
            <Link href={spellingRulePath(rule.slug)} className="font-display text-3xl">{rule.title}</Link>
            {rule.summary ? <p className="mt-2 leading-8">{rule.summary}</p> : null}
          </article>
        )) : <p>لا توجد قواعد منشورة في هذا الموضوع.</p>}
      </section>
      <Sources sources={topic.sources} />
    </main>
  );
}

function Sources({ sources }: { sources: Source[] }) {
  if (!sources.length) return null;
  return (
    <section className="mt-8" aria-labelledby="sources">
      <h2 id="sources" className="font-display text-3xl">المصادر</h2>
      <ul className="mt-3 space-y-2">
        {sources.map((source, index) => (
          <li key={`${source.title}-${index}`}><cite>{[source.title, source.author, source.attribution].filter(Boolean).join(" — ")}</cite></li>
        ))}
      </ul>
    </section>
  );
}
