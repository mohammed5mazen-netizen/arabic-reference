import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { canonicalSlug, publicJson } from "@/lib/dictionary";
import { knowledgeCrumbs, poetryClass } from "@/lib/knowledge";
import { siteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";
type Example = { text?: string; explanation?: string; interpretation?: string; scholarlyNote?: string; alternativeInterpretation?: string; highlightedSegment?: string };
type Device = {
  name: string;
  slug: string;
  shortDefinition: string;
  detailedExplanation?: string | null;
  topic?: { title?: string; slug?: string };
  components: { heading: string; body: string }[];
  examples: Example[];
  relations: { kind: string; title?: string; slug?: string }[];
  sources: { title?: string; author?: string }[];
};

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const device = await publicJson<Device>(`/api/v1/public/rhetoric/devices/${encodeURIComponent(canonicalSlug(slug))}`);
  const title = device?.name ?? "فن بلاغي";
  return { title, description: device?.shortDefinition || title, alternates: { canonical: `${siteUrl}/rhetoric/devices/${slug}` }, openGraph: { title, description: device?.shortDefinition || title, url: `${siteUrl}/rhetoric/devices/${slug}` } };
}

export default async function RhetoricDevicePage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const device = await publicJson<Device>(`/api/v1/public/rhetoric/devices/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!device) notFound();
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("البلاغة", "/rhetoric", [{ label: device.topic?.title ?? "البلاغة", href: device.topic?.slug ? `/rhetoric/${device.topic.slug}` : "/rhetoric" }, { label: device.name }])} />
      <article>
        <p className="text-sm text-muted">فن بلاغي</p>
        <h1 className="font-display text-5xl">{device.name}</h1>
        <p className="mt-4 leading-8">{device.shortDefinition}</p>
        {device.detailedExplanation ? <p className="mt-4 leading-8">{device.detailedExplanation}</p> : null}
        {device.components.map((component) => (
          <section key={component.heading} className="mt-6">
            <h2 className="font-display text-3xl">{component.heading}</h2>
            <p className="mt-2 leading-8">{component.body}</p>
          </section>
        ))}
        {device.examples.map((example, index) => (
          <blockquote key={index} className={`mt-6 border-s-4 border-line ps-4 ${poetryClass}`}>
            <p>{example.text}</p>
            {example.explanation ? <p className="mt-2">{example.explanation}</p> : null}
            {example.interpretation ? <p className="mt-2 text-sm text-muted">قراءة: {example.interpretation}</p> : null}
            {example.alternativeInterpretation ? <p className="mt-2 text-sm text-muted">فهم آخر: {example.alternativeInterpretation}</p> : null}
            {example.scholarlyNote ? <p className="mt-2 text-sm text-muted">{example.scholarlyNote}</p> : null}
          </blockquote>
        ))}
        {device.relations.length ? (
          <nav className="mt-8" aria-label="صلات">
            <h2 className="font-display text-3xl">صلات</h2>
            <ul className="mt-3 space-y-2">{device.relations.map((relation) => (
              <li key={`${relation.kind}-${relation.slug}`}>{relation.slug ? <Link href={`/rhetoric/devices/${relation.slug}`}>{relation.title}</Link> : relation.title}</li>
            ))}</ul>
          </nav>
        ) : null}
        {device.sources.length ? <ul className="mt-8 space-y-2">{device.sources.map((source, index) => <li key={index}><cite>{[source.title, source.author].filter(Boolean).join(" — ")}</cite></li>)}</ul> : null}
      </article>
    </main>
  );
}
