import type { Metadata } from "next";
import Link from "next/link";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { publicJson } from "@/lib/dictionary";
import { emptyLiteratureMessage, knowledgeCrumbs } from "@/lib/knowledge";
import { publicMetadata } from "@/lib/metadata";

export const dynamic = "force-dynamic";
type EraItem = { name: string; slug: string; summary?: string | null };
type LinkItem = { title: string; slug: string; summary?: string | null };

export async function generateMetadata(): Promise<Metadata> {
  return publicMetadata({
    title: "الأدب",
    description: "حقب وأعلام وأعمال من الأدب العربي، ببيانات حقوق واضحة.",
    path: "/literature",
  });
}

export default async function LiteratureHomePage() {
  const [eras, genres] = await Promise.all([
    publicJson<EraItem[]>("/api/v1/public/literature/eras"),
    publicJson<LinkItem[]>("/api/v1/public/literature/genres"),
  ]);
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={knowledgeCrumbs("الأدب", "/literature", [])} />
      <h1 className="font-display text-5xl">الأدب</h1>
      <p className="mt-4 leading-8 text-muted">معرفة عن الحقب والأعلام والأعمال. النص الكامل لا يُعرض إلا إذا سمحت الحقوق.</p>
      <section className="mt-8 space-y-3">
        <h2 className="font-display text-3xl">الحقبات</h2>
        {eras?.length ? eras.map((era) => (
          <article key={era.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
            <Link href={`/literature/eras/${era.slug}`} className="font-display text-3xl">{era.name}</Link>
            {era.summary ? <p className="mt-2 leading-8">{era.summary}</p> : null}
          </article>
        )) : <p>{emptyLiteratureMessage}</p>}
      </section>
      <section className="mt-8">
        <h2 className="font-display text-3xl">الأنواع</h2>
        {genres?.length ? <ul className="mt-3 flex flex-wrap gap-2">{genres.map((genre) => <li key={genre.slug} className="rounded-full border border-line px-4 py-2">{genre.title}</li>)}</ul> : <p className="mt-3">لا توجد أنواع منشورة.</p>}
      </section>
    </main>
  );
}
