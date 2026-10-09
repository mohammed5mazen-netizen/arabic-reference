import type { Metadata } from "next";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { canonicalSlug, publicJson } from "@/lib/dictionary";
import { knowledgeCrumbs, poetryClass, spellingTopicPath } from "@/lib/knowledge";
import { publicMetadata } from "@/lib/metadata";

export const dynamic = "force-dynamic";

type Clause = { kind: string; heading: string; body: string };
type Example = { kind: string; correctForm?: string | null; incorrectForm?: string | null; explanation?: string | null; contextNote?: string | null; commonForm?: string | null; reason?: string | null };
type Source = { title?: string; author?: string };
type Rule = {
  title: string;
  slug: string;
  summary?: string | null;
  coreRule: string;
  difficultyLabel?: string | null;
  topic: { title: string; slug: string };
  clauses: Clause[];
  examples: Example[];
  sources: Source[];
};

const clauseLabel: Record<string, string> = { DEFINITION: "التعريف", CONDITION: "الشرط", EXCEPTION: "الاستثناء", NOTE: "ملاحظة" };

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const rule = await publicJson<Rule>(`/api/v1/public/spelling/rules/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!rule) return publicMetadata({ title: "قاعدة إملائية غير منشورة", description: "هذه القاعدة غير منشورة.", path: "/spelling", index: false });
  return publicMetadata({ title: rule.title, description: rule.summary || rule.coreRule, path: `/spelling/rules/${rule.slug}` });
}

export default async function SpellingRulePage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const rule = await publicJson<Rule>(`/api/v1/public/spelling/rules/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!rule) notFound();
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("الإملاء", "/spelling", [{ label: rule.topic.title, href: spellingTopicPath(rule.topic.slug) }, { label: rule.title }])} />
      <article>
        <p className="text-sm text-muted">قاعدة إملائية{rule.difficultyLabel ? ` · ${rule.difficultyLabel}` : ""}</p>
        <h1 className="font-display text-5xl">{rule.title}</h1>
        {rule.summary ? <p className="mt-4 leading-8">{rule.summary}</p> : null}
        <p className="mt-4 leading-8">{rule.coreRule}</p>
        {rule.clauses.map((clause) => (
          <section key={`${clause.kind}-${clause.heading}`} className="mt-6">
            <h2 className="font-display text-3xl">{clauseLabel[clause.kind] ?? clause.heading}</h2>
            <p className="mt-2 leading-8">{clause.body}</p>
          </section>
        ))}
        {rule.examples.length ? (
          <section className="mt-8 space-y-3" aria-labelledby="examples">
            <h2 id="examples" className="font-display text-3xl">أمثلة</h2>
            {rule.examples.map((example, index) => (
              <div key={index} className="rounded-[1.5rem] border border-line bg-raised p-5">
                {example.commonForm ? <p>الشائع: {example.commonForm}</p> : null}
                {example.correctForm ? <p className={poetryClass}>الصحيح: {example.correctForm}</p> : null}
                {example.incorrectForm ? <p>صورة أخرى: {example.incorrectForm}</p> : null}
                {example.contextNote ? <p className="mt-2 text-sm text-muted">{example.contextNote}</p> : null}
                {example.explanation ? <p className="mt-2 leading-8">{example.explanation}</p> : null}
                {example.reason ? <p className="mt-2 leading-8">{example.reason}</p> : null}
              </div>
            ))}
          </section>
        ) : null}
        {rule.sources.length ? (
          <section className="mt-8">
            <h2 className="font-display text-3xl">المصادر</h2>
            <ul className="mt-3 space-y-2">{rule.sources.map((source, index) => <li key={index}><cite>{[source.title, source.author].filter(Boolean).join(" — ")}</cite></li>)}</ul>
          </section>
        ) : null}
      </article>
    </main>
  );
}
