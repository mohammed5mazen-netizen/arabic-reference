import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { canonicalSlug, publicJson } from "@/lib/dictionary";
import { knowledgeCrumbs, rhetoricDevicePath } from "@/lib/knowledge";
import { siteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";
type Topic = { title: string; slug: string; summary?: string | null; categoryLabel?: string; devices: { title: string; slug: string; summary?: string | null }[] };

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const topic = await publicJson<Topic>(`/api/v1/public/rhetoric/topics/${encodeURIComponent(canonicalSlug(slug))}`);
  const title = topic?.title ?? "موضوع بلاغي";
  return { title, description: topic?.summary || title, alternates: { canonical: `${siteUrl}/rhetoric/${slug}` }, openGraph: { title, description: topic?.summary || title, url: `${siteUrl}/rhetoric/${slug}` } };
}

export default async function RhetoricTopicPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const topic = await publicJson<Topic>(`/api/v1/public/rhetoric/topics/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!topic) notFound();
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("البلاغة", "/rhetoric", [{ label: topic.title }])} />
      <p className="text-sm text-muted">{topic.categoryLabel}</p>
      <h1 className="font-display text-5xl">{topic.title}</h1>
      {topic.summary ? <p className="mt-4 leading-8">{topic.summary}</p> : null}
      <section className="mt-8 space-y-3">
        <h2 className="font-display text-3xl">الفنون</h2>
        {topic.devices.length ? topic.devices.map((device) => (
          <article key={device.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
            <Link href={rhetoricDevicePath(device.slug)} className="font-display text-3xl">{device.title}</Link>
            {device.summary ? <p className="mt-2 leading-8">{device.summary}</p> : null}
          </article>
        )) : <p>لا توجد فنون منشورة في هذا الموضوع.</p>}
      </section>
    </main>
  );
}
