import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { attributionLine, publicJson } from "@/lib/dictionary";
import { exampleCards, grammarAttribution, grammarCrumbs, grammarLabel, grammarSlug, grammarTitle, missingAnnotationMessage, type GrammarExample } from "@/lib/grammar";
import { siteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

type Source = { title?: string | null; author?: string | null; edition?: string | null; publicationYear?: number | null; pageFrom?: number | null; pageTo?: number | null; attributionText?: string | null };
type Rule = {
  title: string;
  slug: string;
  summary?: string | null;
  ruleText?: string | null;
  difficultyLabel?: string | null;
  topic?: { title: string; slug: string } | null;
  components: { type: string; typeLabel?: string | null; heading?: string | null; body: string; sources: Source[] }[];
  examples: GrammarExample[];
  relations: { type: string; typeLabel?: string | null; title: string; slug: string }[];
  concepts: { title: string; slug: string }[];
  sources: Source[];
};

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const rule = await load(params);
  if (!rule) return { title: "قاعدة غير منشورة", robots: { index: false, follow: false } };
  return { title: grammarTitle("rule", rule.title), description: rule.summary ?? rule.ruleText ?? undefined, alternates: { canonical: `${siteUrl}/grammar/rules/${rule.slug}` } };
}

export default async function GrammarRulePage({ params }: { params: Promise<{ slug: string }> }) {
  const rule = await load(params);
  if (!rule) notFound();
  const crumbs = grammarCrumbs([
    ...(rule.topic ? [{ label: rule.topic.title, href: `/grammar/${rule.topic.slug}` }] : []),
    { label: rule.title },
  ]);
  const groups = ["DEFINITION", "CORE_RULE", "CONDITION", "EXCEPTION", "NOTE", "WARNING", "TERMINOLOGY", "DIFFERENCE", "SCHOLARLY_NOTE"];
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={crumbs} />
      {rule.difficultyLabel ? <p className="text-sm text-muted">{rule.difficultyLabel}</p> : null}
      <h1 className="mt-2 font-display text-5xl">{rule.title}</h1>
      {rule.summary ? <p className="mt-4 leading-8">{rule.summary}</p> : null}
      {rule.ruleText ? <p className="mt-4 leading-8">{rule.ruleText}</p> : null}
      {groups.map((type) => {
        const items = rule.components.filter((component) => component.type === type);
        if (items.length === 0) return null;
        return (
          <section key={type} className="mt-8 space-y-3">
            <h2 className="font-display text-3xl">{grammarLabel(type)}</h2>
            {items.map((component) => (
              <article key={`${component.type}-${component.body}`} className="rounded-[1.5rem] border border-line bg-raised p-5">
                {component.heading ? <h3 className="font-display text-2xl">{component.heading}</h3> : null}
                <p className="mt-2 leading-8">{component.body}</p>
                {component.sources.map((source) => <p key={attributionLine(source)} className="mt-3 text-sm text-muted">{attributionLine(source)}</p>)}
              </article>
            ))}
          </section>
        );
      })}
      <section className="mt-8 space-y-4">
        <h2 className="font-display text-3xl">الأمثلة</h2>
        {rule.examples.map((example) => {
          const presentation = exampleCards(example);
          return (
            <article key={`${example.exampleType}-${example.textOriginal}`} className="rounded-[1.5rem] border border-line bg-raised p-5">
              <p className="text-sm text-muted">{example.exampleTypeLabel || grammarLabel(example.exampleType)}</p>
              <p className="mt-2 font-display text-3xl leading-relaxed">{example.textOriginal}</p>
              {example.explanation ? <p className="mt-3 leading-8">{example.explanation}</p> : null}
              {example.editorialNote ? <p className="mt-2 text-sm text-muted">{example.editorialNote}</p> : null}
              {example.surah ? <p className="mt-2 text-sm">سورة {example.surah}، الآية {example.ayah}</p> : null}
              {example.poet ? <p className="mt-2 text-sm">{[example.poet, example.workTitle, example.verseLocator].filter(Boolean).join(" · ")}</p> : null}
              {example.source ? <p className="mt-3 text-sm text-muted">{grammarAttribution(example.source)}</p> : null}
              {presentation.cards.length > 0 ? (
                <ul className="mt-4 grid gap-3 sm:grid-cols-2">
                  {presentation.cards.map((card) => (
                    <li key={card.surface} className="rounded-2xl border border-line p-4">
                      <p className="font-display text-3xl">{card.surface}</p>
                      {card.lines.map((line) => <p key={line} className="mt-1 text-sm">{line}</p>)}
                    </li>
                  ))}
                </ul>
              ) : null}
              {presentation.note ? <p className="mt-3">{presentation.note || missingAnnotationMessage}</p> : null}
              {example.tokens?.map((token) => token.lexical ? (
                <Link key={token.lexical.slug} href={`/word/${token.lexical.slug}`} className="mt-3 inline-block text-library">{token.lexical.lemma}</Link>
              ) : null)}
              {example.tokens?.some((token) => token.morphology) ? <p className="mt-2 text-sm">عرض التحليل الصرفي</p> : null}
            </article>
          );
        })}
      </section>
      {rule.concepts.length > 0 ? (
        <section className="mt-8">
          <h2 className="font-display text-3xl">مصطلحات مرتبطة</h2>
          <ul className="mt-3 space-y-2">{rule.concepts.map((concept) => <li key={concept.slug}><Link href={`/grammar/concepts/${concept.slug}`}>{concept.title}</Link></li>)}</ul>
        </section>
      ) : null}
      {rule.relations.length > 0 ? (
        <section className="mt-8">
          <h2 className="font-display text-3xl">موضوعات ذات صلة</h2>
          <ul className="mt-3 space-y-2">{rule.relations.map((relation) => <li key={relation.slug}><Link href={`/grammar/rules/${relation.slug}`}>{relation.typeLabel || grammarLabel(relation.type)}: {relation.title}</Link></li>)}</ul>
        </section>
      ) : null}
      {rule.sources.length > 0 ? (
        <section className="mt-8">
          <h2 className="font-display text-3xl">المصادر</h2>
          <ul className="mt-3 space-y-2">{rule.sources.map((source) => <li key={attributionLine(source)}>{attributionLine(source)}</li>)}</ul>
        </section>
      ) : null}
    </main>
  );
}

async function load(params: Promise<{ slug: string }>): Promise<Rule | null> {
  const { slug } = await params;
  return publicJson<Rule>(`/api/v1/public/grammar/rules/${encodeURIComponent(grammarSlug(slug))}`);
}
