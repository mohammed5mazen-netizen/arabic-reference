import type { Metadata } from "next";
import Link from "next/link";
import { notFound } from "next/navigation";
import { connection } from "next/server";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { LearnStatus } from "@/components/learning-progress";
import { LessonQuiz } from "@/components/lesson-quiz";
import { publicJson } from "@/lib/dictionary";
import { assistantLessonQuestion, durationLabel, learningSlug, lessonHref, type LearningLesson, type LearningPath } from "@/lib/learning";
import { resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

export async function generateMetadata({ params }: { params: Promise<{ pathSlug: string; lessonSlug: string }> }): Promise<Metadata> {
  await connection();
  const pathSlug = learningSlug((await params).pathSlug);
  const lessonSlug = learningSlug((await params).lessonSlug);
  const lesson = await publicJson<LearningLesson>(`/api/v1/public/learning/lessons/${encodeURIComponent(lessonSlug)}`);
  const title = lesson?.title ?? "درس";
  return {
    title,
    description: lesson?.summary ?? title,
    alternates: { canonical: `${resolveSiteUrl()}/learn/${encodeURIComponent(pathSlug)}/${encodeURIComponent(lessonSlug)}` },
    robots: { index: true, follow: true },
  };
}

export default async function LessonPage({ params }: { params: Promise<{ pathSlug: string; lessonSlug: string }> }) {
  const pathSlug = learningSlug((await params).pathSlug);
  const lessonSlug = learningSlug((await params).lessonSlug);
  const lesson = await publicJson<LearningLesson>(`/api/v1/public/learning/lessons/${encodeURIComponent(lessonSlug)}`);
  if (!lesson || lesson.pathSlug !== pathSlug) notFound();
  const path = await publicJson<LearningPath>(`/api/v1/public/learning/paths/${encodeURIComponent(pathSlug)}`);
  const lessonSlugs = path?.units.flatMap((unit) => unit.lessons.map((item) => item.slug)) ?? [lesson.slug];

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-6xl px-5 py-10">
      <div className="grid gap-8 lg:grid-cols-[16rem_minmax(0,42rem)]">
        <aside>
          <details className="rounded-3xl border border-line bg-raised p-4 lg:hidden">
            <summary className="min-h-12 cursor-pointer">دروس المسار</summary>
            <LessonList path={path} current={lesson.slug} />
          </details>
          <nav aria-label="دروس المسار" className="sticky top-6 hidden rounded-3xl border border-line bg-raised p-4 lg:block">
            <p className="font-display text-2xl">{lesson.pathTitle}</p>
            <LessonList path={path} current={lesson.slug} />
          </nav>
        </aside>
        <article className="min-w-0">
          <Breadcrumbs items={[{ label: "الرئيسية", href: "/" }, { label: "التعلّم", href: "/learn" }, { label: lesson.pathTitle, href: `/learn/${lesson.pathSlug}` }, { label: lesson.title }]} />
          <p className="text-sm text-muted">{durationLabel(lesson.estimatedMinutes)}</p>
          <h1 className="mt-2 font-display text-5xl leading-tight">{lesson.title}</h1>
          <p className="mt-4 text-lg leading-9">{lesson.summary}</p>
          <section className="mt-8" aria-labelledby="objectives">
            <h2 id="objectives" className="font-display text-3xl">بعد هذا الدرس يستطيع المتعلم</h2>
            <ul className="mt-3 list-disc space-y-2 pe-5 leading-8">
              {lesson.objectives.map((objective) => <li key={objective}>{objective}</li>)}
            </ul>
          </section>
          <div className="mt-8 space-y-6">
            {lesson.sections.map((section) => (
              <section key={`${section.typeLabel}-${section.heading}`} className="leading-9">
                <p className="text-sm text-library">{section.typeLabel}</p>
                <h2 className="font-display text-3xl">{section.heading}</h2>
                {section.exampleLabel ? <p className="mt-2 text-sm">{section.exampleLabel}</p> : null}
                <p className="mt-2 whitespace-pre-wrap">{section.body}</p>
              </section>
            ))}
          </div>
          {lesson.references.length ? (
            <section className="mt-8" aria-labelledby="references">
              <h2 id="references" className="font-display text-3xl">من المرجع</h2>
              <ul className="mt-3 space-y-3">
                {lesson.references.map((reference) => (
                  <li key={`${reference.kindLabel}-${reference.title}`} className="rounded-2xl border border-line p-4">
                    <p className="text-sm text-muted">{reference.kindLabel}</p>
                    {reference.href ? <Link href={reference.href} className="font-display text-2xl text-library">{reference.title}</Link> : <p className="font-display text-2xl">{reference.title}</p>}
                    {reference.note ? <p className="mt-2 leading-8">{reference.note}</p> : null}
                  </li>
                ))}
              </ul>
            </section>
          ) : null}
          {lesson.activities.length ? (
            <section className="mt-8 space-y-3" aria-labelledby="activities">
              <h2 id="activities" className="font-display text-3xl">نشاط</h2>
              {lesson.activities.map((activity) => (
                <article key={activity.title} className="rounded-2xl border border-line p-4">
                  <p className="text-sm text-muted">{activity.typeLabel}</p>
                  <h3 className="font-display text-2xl">{activity.title}</h3>
                  <p className="mt-2 leading-8">{activity.instructions}</p>
                </article>
              ))}
            </section>
          ) : null}
          <p className="mt-8">
            <Link href={assistantLessonQuestion(lesson.title)} className="text-library">اسأل المساعد عن هذا الدرس</Link>
          </p>
          <nav aria-label="الدرس السابق والتالي" className="mt-8 flex flex-col gap-3 sm:flex-row sm:justify-between">
            {lesson.previous ? <Link href={lessonHref(lesson.pathSlug, lesson.previous.slug)} className="min-h-12 text-library">السابق: {lesson.previous.title}</Link> : <span />}
            {lesson.next ? <Link href={lessonHref(lesson.pathSlug, lesson.next.slug)} className="min-h-12 text-library">التالي: {lesson.next.title}</Link> : null}
          </nav>
          {lesson.quiz ? <LessonQuiz quiz={lesson.quiz} /> : null}
          <LearnStatus pathSlug={lesson.pathSlug} lessonSlug={lesson.slug} title={lesson.title} lessonSlugs={lessonSlugs} />
        </article>
      </div>
    </main>
  );
}

function LessonList({ path, current }: { path: LearningPath | null; current: string }) {
  if (!path) return null;
  return (
    <ol className="mt-3 space-y-2 text-sm leading-7">
      {path.units.flatMap((unit) => unit.lessons).map((item) => (
        <li key={item.slug}>
          {item.slug === current ? <span aria-current="page">{item.title}</span> : <Link href={lessonHref(path.slug, item.slug)}>{item.title}</Link>}
        </li>
      ))}
    </ol>
  );
}
