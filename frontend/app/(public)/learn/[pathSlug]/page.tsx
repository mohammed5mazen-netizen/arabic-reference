import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { LearnStatus } from "@/components/learning-progress";
import { publicJson } from "@/lib/dictionary";
import { durationLabel, learningSlug, lessonHref, type LearningPath } from "@/lib/learning";
import { resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

export async function generateMetadata({ params }: { params: Promise<{ pathSlug: string }> }): Promise<Metadata> {
  await connection();
  const pathSlug = learningSlug((await params).pathSlug);
  const path = await publicJson<LearningPath>(`/api/v1/public/learning/paths/${encodeURIComponent(pathSlug)}`);
  const title = path?.title ?? "مسار تعليمي";
  return {
    title,
    description: path?.summary ?? title,
    alternates: { canonical: `${resolveSiteUrl()}/learn/${encodeURIComponent(pathSlug)}` },
    robots: { index: true, follow: true },
  };
}

export default async function LearningPathPage({ params }: { params: Promise<{ pathSlug: string }> }) {
  const pathSlug = learningSlug((await params).pathSlug);
  const path = await publicJson<LearningPath>(`/api/v1/public/learning/paths/${encodeURIComponent(pathSlug)}`);
  if (!path) notFound();
  const lessonSlugs = path.units.flatMap((unit) => unit.lessons.map((lesson) => lesson.slug));

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={[{ label: "الرئيسية", href: "/" }, { label: "التعلّم", href: "/learn" }, { label: path.title }]} />
      <p className="text-sm text-muted">{path.difficultyLabel}{durationLabel(path.estimatedMinutes) ? ` · ${durationLabel(path.estimatedMinutes)}` : ""}</p>
      <h1 className="mt-2 font-display text-5xl">{path.title}</h1>
      <p className="mt-4 leading-8">{path.description}</p>
      {path.prerequisite ? (
        <p className="mt-4">قبل هذا المسار: <Link href={`/learn/${path.prerequisite.slug}`} className="text-library">{path.prerequisite.title}</Link></p>
      ) : null}
      <LearnStatus pathSlug={path.slug} title={path.title} lessonSlugs={lessonSlugs} />
      <div className="mt-10 space-y-8">
        {path.units.map((unit) => (
          <section key={unit.title} aria-labelledby={unit.title}>
            <h2 id={unit.title} className="font-display text-3xl">{unit.title}</h2>
            <p className="mt-2 leading-8 text-muted">{unit.summary}</p>
            <ol className="mt-4 space-y-3">
              {unit.lessons.map((lesson, index) => (
                <li key={lesson.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
                  <p className="text-sm text-muted">الدرس {index + 1}{durationLabel(lesson.estimatedMinutes) ? ` · ${durationLabel(lesson.estimatedMinutes)}` : ""}</p>
                  <Link href={lessonHref(path.slug, lesson.slug)} className="font-display text-2xl">{lesson.title}</Link>
                  <p className="mt-2 leading-8">{lesson.summary}</p>
                </li>
              ))}
            </ol>
          </section>
        ))}
      </div>
    </main>
  );
}
