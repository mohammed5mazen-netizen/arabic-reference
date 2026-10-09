import type { Metadata } from "next";
import Link from "next/link";
import { notFound, permanentRedirect } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { canonicalSlug, publicJson } from "@/lib/dictionary";
import { knowledgeCrumbs } from "@/lib/knowledge";
import { publicMetadata } from "@/lib/metadata";
import { articleJsonLd, describe, jsonLd } from "@/lib/seo";
import { absoluteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";
type Article = {
  title: string;
  slug: string;
  excerpt?: string | null;
  editorName?: string | null;
  publishedAt?: string | null;
  modifiedAt?: string | null;
  sections: { heading?: string; body?: string }[];
  tags: string[];
  relations: { title?: string; url?: string }[];
  sources: { title?: string; author?: string }[];
};

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const article = await publicJson<Article>(`/api/v1/public/articles/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!article) return publicMetadata({ title: "مقالة غير منشورة", description: "هذه المقالة غير منشورة.", path: "/articles", index: false });
  return publicMetadata({
    title: article.title,
    description: article.excerpt || article.title,
    path: `/articles/${article.slug}`,
    type: "article",
  });
}

export default async function ArticlePage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const article = await publicJson<Article>(`/api/v1/public/articles/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!article) notFound();
  if (canonicalSlug(slug) !== article.slug) permanentRedirect(`/articles/${article.slug}`);
  const lessons = await publicJson<Array<{ title: string; pathSlug: string; lessonSlug: string }>>(`/api/v1/public/learning/references?kind=ARTICLE&slug=${encodeURIComponent(article.slug)}`);
  const description = describe(article.excerpt, article.title);
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("المقالات", "/articles", [{ label: article.title }])} />
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: jsonLd(articleJsonLd({ headline: article.title, description, url: absoluteUrl(`/articles/${article.slug}`), publishedAt: article.publishedAt, modifiedAt: article.modifiedAt, author: article.editorName })) }} />
      <article>
        <h1 className="font-display text-5xl">{article.title}</h1>
        {article.editorName ? <p className="mt-2 text-sm text-muted">{article.editorName}</p> : null}
        {article.excerpt ? <p className="mt-4 leading-8">{article.excerpt}</p> : null}
        {article.sections.map((section, index) => (
          <section key={index} className="mt-8">
            {section.heading ? <h2 className="font-display text-3xl">{section.heading}</h2> : null}
            <p className="mt-2 leading-8">{section.body}</p>
          </section>
        ))}
        {article.relations.length ? (
          <nav className="mt-8" aria-label="معرفة مرتبطة">
            <h2 className="font-display text-3xl">معرفة مرتبطة</h2>
            <ul className="mt-3 space-y-2">{article.relations.map((relation) => <li key={relation.url}>{relation.url ? <Link href={relation.url}>{relation.title}</Link> : relation.title}</li>)}</ul>
          </nav>
        ) : null}
        {lessons?.length ? (
          <nav className="mt-8" aria-label="دروس مرتبطة">
            <h2 className="font-display text-3xl">دروس مرتبطة</h2>
            <ul className="mt-3 space-y-2">{lessons.map((lesson) => <li key={lesson.lessonSlug}><Link href={`/learn/${lesson.pathSlug}/${lesson.lessonSlug}`}>{lesson.title}</Link></li>)}</ul>
          </nav>
        ) : null}
        {article.sources.length ? <ul className="mt-8">{article.sources.map((source, index) => <li key={index}><cite>{[source.title, source.author].filter(Boolean).join(" — ")}</cite></li>)}</ul> : null}
      </article>
    </main>
  );
}
