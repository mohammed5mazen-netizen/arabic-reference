import type { Metadata } from "next";
import Link from "next/link";
import { connection } from "next/server";
import { Breadcrumbs } from "@/components/breadcrumbs";
import { LearnStatus } from "@/components/learning-progress";
import { publicJson } from "@/lib/dictionary";
import { durationLabel, type LearningPathCard } from "@/lib/learning";
import { resolveSiteUrl } from "@/lib/site";

export const dynamic = "force-dynamic";

export async function generateMetadata(): Promise<Metadata> {
  await connection();
  return {
    title: "التعلّم",
    description: "مسارات عربية قصيرة مبنية على معرفة المرجع المنشورة.",
    alternates: { canonical: `${resolveSiteUrl()}/learn` },
    robots: { index: true, follow: true },
  };
}

export default async function LearnPage() {
  const paths = (await publicJson<LearningPathCard[]>("/api/v1/public/learning/paths")) ?? [];
  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <Breadcrumbs items={[{ label: "الرئيسية", href: "/" }, { label: "التعلّم" }]} />
      <p className="text-sm text-library">قراءة مفتوحة</p>
      <h1 className="mt-2 font-display text-5xl sm:text-6xl">تعلّم العربية من المرجع</h1>
      <p className="mt-4 max-w-2xl leading-8 text-muted">
        مسارات قصيرة تُحيل إلى المعرفة المنشورة. اقرأ الدرس وأجب عن الاختبار من دون حساب. حفظ التقدّم يبقى على هذا الجهاز إذا أردت.
      </p>
      <LearnStatus lessonSlugs={[]} />
      <section className="mt-10 space-y-4" aria-labelledby="paths-title">
        <h2 id="paths-title" className="font-display text-3xl">المسارات</h2>
        {paths.length ? paths.map((path) => (
          <article key={path.slug} className="rounded-[1.5rem] border border-line bg-raised p-5">
            <p className="text-sm text-muted">{path.difficultyLabel}{durationLabel(path.estimatedMinutes) ? ` · ${durationLabel(path.estimatedMinutes)}` : ""}</p>
            <Link href={`/learn/${path.slug}`} className="mt-1 block font-display text-3xl">{path.title}</Link>
            <p className="mt-2 leading-8">{path.summary}</p>
          </article>
        )) : <p>لا توجد مسارات منشورة بعد.</p>}
      </section>
    </main>
  );
}
