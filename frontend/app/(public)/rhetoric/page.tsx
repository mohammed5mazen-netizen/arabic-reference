import type { Metadata } from "next";
import Link from "next/link";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { publicJson } from "@/lib/dictionary";
import { emptyRhetoricMessage, knowledgeCrumbs } from "@/lib/knowledge";
import { publicMetadata } from "@/lib/metadata";

export const dynamic = "force-dynamic";
type LinkItem = { title: string; slug: string; summary?: string | null };

export async function generateMetadata(): Promise<Metadata> {
  return publicMetadata({
    title: "البلاغة",
    description: "علم المعاني والبيان والبديع، بأجهزة بلاغية موثّقة.",
    path: "/rhetoric",
  });
}

export default async function RhetoricHomePage() {
  const topics = await publicJson<LinkItem[]>("/api/v1/public/rhetoric/topics");
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("البلاغة", "/rhetoric", [])} />
      <h1 className="font-display text-5xl">البلاغة</h1>
      <p className="mt-4 leading-8 text-muted">التفسير البلاغي يُعرض مع مصدره، وقد يجاوره فهم آخر.</p>
      <section className="mt-8 space-y-3">
        <h2 className="font-display text-3xl">الموضوعات</h2>
        {topics?.length ? topics.map((topic) => (
          <article key={topic.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
            <Link href={`/rhetoric/${topic.slug}`} className="font-display text-3xl">{topic.title}</Link>
            {topic.summary ? <p className="mt-2 leading-8">{topic.summary}</p> : null}
          </article>
        )) : <p>{emptyRhetoricMessage}</p>}
      </section>
    </main>
  );
}
