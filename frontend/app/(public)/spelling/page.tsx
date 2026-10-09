import type { Metadata } from "next";
import Link from "next/link";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { publicJson } from "@/lib/dictionary";
import { emptySpellingMessage, knowledgeCrumbs, spellingTopicPath } from "@/lib/knowledge";
import { publicMetadata } from "@/lib/metadata";

export const dynamic = "force-dynamic";

type LinkItem = { title: string; slug: string; summary?: string | null };

export async function generateMetadata(): Promise<Metadata> {
  return publicMetadata({
    title: "الإملاء",
    description: "قواعد الكتابة العربية: الهمزة، والألف اللينة، والتاء، موثّقة بمصادرها.",
    path: "/spelling",
  });
}

export default async function SpellingHomePage() {
  const topics = await publicJson<LinkItem[]>("/api/v1/public/spelling/topics");
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("الإملاء", "/spelling", [])} />
      <h1 className="font-display text-5xl">الإملاء</h1>
      <p className="mt-4 leading-8 text-muted">قواعد الكتابة العربية في موضوعات وقواعد وأمثلة، ولا تُنشر قاعدة بلا مصدر.</p>
      <section className="mt-8 space-y-3" aria-labelledby="spelling-topics">
        <h2 id="spelling-topics" className="font-display text-3xl">الموضوعات</h2>
        {topics?.length ? topics.map((topic) => (
          <article key={topic.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
            <Link href={spellingTopicPath(topic.slug)} className="font-display text-3xl">{topic.title}</Link>
            {topic.summary ? <p className="mt-2 leading-8">{topic.summary}</p> : null}
          </article>
        )) : <p>{emptySpellingMessage}</p>}
      </section>
    </main>
  );
}
