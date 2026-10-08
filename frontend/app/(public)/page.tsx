import Link from "next/link";
import { SearchPanel } from "@/components/search-panel";
import { LinkButton } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { publicJson } from "@/lib/dictionary";
import { durationLabel, type LearningPathCard } from "@/lib/learning";
import { knowledgeAreas } from "@/lib/navigation";
import { siteName, siteTagline } from "@/lib/site";
import { featuredTools } from "@/lib/tools";

export const dynamic = "force-dynamic";

const areaCopy: Record<string, string> = {
  "/search": "ابحث عن الكلمة، المعنى، والجذر في المعرفة المنشورة.",
  "/grammar": "موضوعات وقواعد ومصطلحات موثّقة.",
  "/tools/morphology": "تحليل صرفي محدود، مع تمييز الموثّق عن المحتمل.",
  "/spelling": "قواعد الكتابة والهمزة والفرق بينها.",
  "/rhetoric": "فنون البلاغة بتعريف ومثال ومصدر.",
  "/literature": "أعلام وأعمال وحقب، من غير نصوص محمية طويلة.",
};

export default async function HomePage() {
  let paths: LearningPathCard[] = [];
  try {
    paths = (await publicJson<LearningPathCard[]>("/api/v1/public/learning/paths")) ?? [];
  } catch {
    paths = [];
  }

  return (
    <main id="content">
      <section id="top" className="relative z-10 mx-auto w-full max-w-6xl px-5 pt-10 sm:pt-16">
        <div className="max-w-3xl">
          <p className="text-sm font-medium text-library">مرجع عربي رقمي مفتوح</p>
          <h1 className="type-display mt-4 font-display sm:text-7xl">{siteName}</h1>
          <p className="mt-4 font-display text-3xl sm:text-4xl">{siteTagline}</p>
          <p className="reading mt-5 text-lg text-muted">ادخل مباشرة إلى اللغة: ابحث، اقرأ، وافتح الكلمة والجذر والقاعدة. لا حساب، ولا حاجز قبل الصفحة.</p>
        </div>
        <SearchPanel />
      </section>

      <section aria-labelledby="areas-title" className="mx-auto mt-14 w-full max-w-6xl px-5">
        <h2 id="areas-title" className="font-display text-4xl">أقسام اللغة</h2>
        <ul className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {knowledgeAreas.map((area) => (
            <li key={area.href}>
              <Card className="h-full">
                <h3 className="font-display text-3xl"><Link href={area.href}>{area.label}</Link></h3>
                <p className="mt-2 leading-7 text-muted">{areaCopy[area.href]}</p>
              </Card>
            </li>
          ))}
        </ul>
      </section>

      <section aria-labelledby="tools-title" className="mx-auto mt-14 w-full max-w-6xl px-5">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <h2 id="tools-title" className="font-display text-4xl">أدوات لغوية</h2>
          <LinkButton href="/tools" variant="link">استكشف جميع الأدوات</LinkButton>
        </div>
        <ul className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {featuredTools.map((tool) => (
            <li key={tool.code}>
              <Card className="h-full">
                <h3 className="font-display text-2xl">{tool.name}</h3>
                <p className="mt-2 text-sm leading-7 text-muted">{tool.description}</p>
                <Link href={tool.route} className="mt-4 inline-flex min-h-11 items-center text-library">استخدام الأداة</Link>
              </Card>
            </li>
          ))}
        </ul>
      </section>

      <section aria-labelledby="learn-title" className="mx-auto mt-14 w-full max-w-6xl px-5">
        <h2 id="learn-title" className="font-display text-4xl">تعلّم العربية</h2>
        {paths.length ? (
          <ul className="mt-5 grid gap-4 sm:grid-cols-2">
            {paths.map((path) => (
              <li key={path.slug}>
                <Card>
                  <p className="text-sm text-muted">{path.difficultyLabel}{durationLabel(path.estimatedMinutes) ? ` · ${durationLabel(path.estimatedMinutes)}` : ""}</p>
                  <h3 className="mt-1 font-display text-3xl"><Link href={`/learn/${path.slug}`}>{path.title}</Link></h3>
                  <p className="mt-2 leading-8">{path.summary}</p>
                </Card>
              </li>
            ))}
          </ul>
        ) : (
          <p className="reading mt-4 text-muted">المسارات المنشورة تظهر هنا عندما يراجعها فريق التحرير. يمكنك فتح صفحة التعلّم الآن.</p>
        )}
        <LinkButton href="/learn" variant="secondary" className="mt-4">ابدأ التعلّم</LinkButton>
      </section>

      <section aria-labelledby="assistant-home" className="mx-auto mt-14 w-full max-w-6xl px-5">
        <Card className="max-w-3xl">
          <h2 id="assistant-home" className="font-display text-4xl">اسأل المساعد اللغوي</h2>
          <p className="mt-3 leading-8 text-muted">سؤال، ثم إجابة، ثم الأدلة المنشورة. إذا كان المساعد متوقفًا تبقى البحث والأدوات متاحتين.</p>
          <Link href="/assistant" className="mt-4 inline-flex min-h-11 items-center text-library">افتح المساعد اللغوي</Link>
        </Card>
      </section>

      <section aria-labelledby="trust-title" className="mx-auto mt-14 w-full max-w-6xl px-5 pb-8">
        <h2 id="trust-title" className="font-display text-4xl">ما الذي يُعرض؟</h2>
        <ul className="mt-4 max-w-3xl space-y-3 leading-8 text-muted">
          <li>المعرفة المنشورة تمر بمراجعة تحريرية وتُنسب إلى مصدرها.</li>
          <li>التحليل المحتمل، مثل بعض القراءات الصرفية، يبقى موسومًا ولا يُقدَّم كقاعدة موثّقة.</li>
          <li>المساعد لا ينشر محتوى، ولا يصحّح الاختبارات.</li>
        </ul>
      </section>
    </main>
  );
}
