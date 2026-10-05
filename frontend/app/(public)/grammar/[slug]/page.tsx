import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { attributionLine, publicJson } from "@/lib/dictionary";
import { grammarCrumbs, grammarSlug, grammarTitle, type GrammarCrumb } from "@/lib/grammar";
import { siteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

type LinkItem = { title: string; slug: string; summary?: string | null; difficultyLabel?: string | null };
type Source = { title?: string | null; author?: string | null; edition?: string | null; publicationYear?: number | null; pageFrom?: number | null; pageTo?: number | null };
type Topic = {
  title: string;
  slug: string;
  summary?: string | null;
  categoryLabel?: string | null;
  difficultyLabel?: string | null;
  ancestors: LinkItem[];
  children: LinkItem[];
  rules: LinkItem[];
  prerequisites: LinkItem[];
  sources: Source[];
};

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const topic = await load(params);
  if (!topic) return { title: "موضوع غير منشور", robots: { index: false, follow: false } };
  return {
    title: grammarTitle("topic", topic.title),
    description: topic.summary ?? undefined,
    alternates: { canonical: `${siteUrl}/grammar/${topic.slug}` },
  };
}

export default async function GrammarTopicPage({ params }: { params: Promise<{ slug: string }> }) {
  const topic = await load(params);
  if (!topic) notFound();
  const crumbs: GrammarCrumb[] = grammarCrumbs([
    ...topic.ancestors.map((item) => ({ label: item.title, href: `/grammar/${item.slug}` })),
    { label: topic.title },
  ]);
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={crumbs} />
      <p className="text-sm text-muted">{[topic.categoryLabel, topic.difficultyLabel].filter(Boolean).join(" · ")}</p>
      <h1 className="mt-2 font-display text-5xl">{topic.title}</h1>
      {topic.summary ? <p className="mt-4 leading-8">{topic.summary}</p> : null}
      <Section title="قبل هذا الموضوع" items={topic.prerequisites} />
      <Section title="موضوعات فرعية" items={topic.children} />
      <section className="mt-8 space-y-3">
        <h2 className="font-display text-3xl">القواعد</h2>
        {topic.rules.length === 0 ? <p>لا توجد قواعد منشورة في هذا الموضوع.</p> : topic.rules.map((rule) => (
          <article key={rule.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
            <Link href={`/grammar/rules/${rule.slug}`} className="font-display text-3xl">{rule.title}</Link>
            {rule.summary ? <p className="mt-2 leading-8">{rule.summary}</p> : null}
          </article>
        ))}
      </section>
      <Sources sources={topic.sources} />
    </main>
  );
}

function Section({ title, items }: { title: string; items: LinkItem[] }) {
  if (items.length === 0) return null;
  return (
    <section className="mt-8 space-y-3">
      <h2 className="font-display text-3xl">{title}</h2>
      {items.map((item) => (
        <article key={item.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
          <Link href={`/grammar/${item.slug}`} className="font-display text-3xl">{item.title}</Link>
          {item.summary ? <p className="mt-2 leading-8">{item.summary}</p> : null}
        </article>
      ))}
    </section>
  );
}

function Sources({ sources }: { sources: Source[] }) {
  if (sources.length === 0) return null;
  return (
    <section className="mt-8">
      <h2 className="font-display text-3xl">المصادر</h2>
      <ul className="mt-3 space-y-2">
        {sources.map((source) => <li key={attributionLine(source)}>{attributionLine(source)}</li>)}
      </ul>
    </section>
  );
}

async function load(params: Promise<{ slug: string }>): Promise<Topic | null> {
  const { slug } = await params;
  return publicJson<Topic>(`/api/v1/public/grammar/topics/${encodeURIComponent(grammarSlug(slug))}`);
}
