import type { Metadata } from "next";
import Link from "next/link";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { publicJson } from "@/lib/dictionary";
import { articlePath, emptyArticlesMessage, knowledgeCrumbs } from "@/lib/knowledge";
import { resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";
type Item = { title: string; slug: string; excerpt?: string | null };

export const metadata: Metadata = {
  title: "المقالات",
  description: "مقالات معرفية منشورة عن العربية.",
  alternates: { canonical: `${resolveSiteUrl()}/articles` },
  openGraph: { title: "المقالات | المرجع العربي", description: "مقالات معرفية منشورة.", url: `${resolveSiteUrl()}/articles` },
};

export default async function ArticlesPage() {
  const articles = await publicJson<Item[]>("/api/v1/public/articles");
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("المقالات", "/articles", [])} />
      <h1 className="font-display text-5xl">المقالات</h1>
      <section className="mt-8 space-y-3">
        {articles?.length ? articles.map((article) => (
          <article key={article.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
            <Link href={articlePath(article.slug)} className="font-display text-3xl">{article.title}</Link>
            {article.excerpt ? <p className="mt-2 leading-8">{article.excerpt}</p> : null}
          </article>
        )) : <p>{emptyArticlesMessage}</p>}
      </section>
    </main>
  );
}
