import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { canonicalSlug, publicJson } from "@/lib/dictionary";
import { emptyWorksMessage, knowledgeCrumbs, literaryRoleLabel, literatureWorkPath } from "@/lib/knowledge";
import { resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";
type Figure = {
  name: string;
  biography?: string | null;
  birthLabel?: string | null;
  deathLabel?: string | null;
  aliases: { alias?: string; kind?: string }[];
  roles: string[];
  eras: { name?: string; slug?: string }[];
  schools: { name?: string }[];
  works: { title?: string; slug?: string }[];
  sources: { title?: string; author?: string }[];
};

export async function generateMetadata({ params }: { params: Promise<{ slug: string }> }): Promise<Metadata> {
  const { slug } = await params;
  const figure = await publicJson<Figure>(`/api/v1/public/literature/figures/${encodeURIComponent(canonicalSlug(slug))}`);
  const title = figure?.name ?? "أديب";
  return { title, description: figure?.biography || title, alternates: { canonical: `${resolveSiteUrl()}/literature/figures/${slug}` }, openGraph: { title, description: figure?.biography || title, url: `${resolveSiteUrl()}/literature/figures/${slug}` } };
}

export default async function FigurePage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = await params;
  const figure = await publicJson<Figure>(`/api/v1/public/literature/figures/${encodeURIComponent(canonicalSlug(slug))}`);
  if (!figure) notFound();
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("الأدب", "/literature", [{ label: figure.name }])} />
      <article>
        <h1 className="font-display text-5xl">{figure.name}</h1>
        <p className="mt-3 text-muted">{[figure.birthLabel, figure.deathLabel].filter(Boolean).join(" — ")}</p>
        {figure.roles.length ? <p className="mt-2">{figure.roles.map(literaryRoleLabel).join(" · ")}</p> : null}
        {figure.eras.length ? <p className="mt-2">العصر: {figure.eras.map((era) => era.name).filter(Boolean).join("، ")}</p> : null}
        {figure.schools.length ? <p className="mt-2">المدرسة: {figure.schools.map((school) => school.name).filter(Boolean).join("، ")}</p> : null}
        {figure.aliases.length ? <p className="mt-2">يعرف أيضًا: {figure.aliases.map((alias) => alias.alias).filter(Boolean).join("، ")}</p> : null}
        {figure.biography ? <p className="mt-4 leading-8">{figure.biography}</p> : null}
        <section className="mt-8">
          <h2 className="font-display text-3xl">الأعمال</h2>
          {figure.works.length ? <ul className="mt-3 space-y-2">{figure.works.map((work) => <li key={work.slug}>{work.slug ? <Link href={literatureWorkPath(work.slug)}>{work.title}</Link> : work.title}</li>)}</ul> : <p className="mt-3">{emptyWorksMessage}</p>}
        </section>
        {figure.sources.length ? <ul className="mt-8">{figure.sources.map((source, index) => <li key={index}><cite>{[source.title, source.author].filter(Boolean).join(" — ")}</cite></li>)}</ul> : null}
      </article>
    </main>
  );
}
