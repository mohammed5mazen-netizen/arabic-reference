import Link from "next/link";
import { SearchPanel } from "@/components/search-panel";
import { SectionGrid } from "@/components/section-grid";
import { featuredTools } from "@/lib/tools";
import { siteName, siteTagline } from "@/lib/site";

export default function HomePage() {
  return (
    <main id="content">
      <section id="top" className="relative z-10 mx-auto w-full max-w-6xl px-5 pt-10 sm:pt-16">
        <div className="max-w-3xl">
          <p className="text-sm font-medium tracking-wide text-library">مرجع عربي رقمي مفتوح</p>
          <h1 className="mt-4 font-display text-6xl leading-tight sm:text-7xl">{siteName}</h1>
          <p className="mt-4 font-display text-3xl text-ink sm:text-4xl">{siteTagline}</p>
          <p className="mt-5 max-w-2xl text-lg leading-8 text-muted">
            ادخل مباشرة إلى اللغة: ابحث، اقرأ، وافتح الكلمة والجذر والقاعدة عندما تكتمل أبواب المعرفة. لا حساب، ولا
            حاجز قبل الصفحة.
          </p>
        </div>
        <SearchPanel />
        <section aria-labelledby="tools-title" className="mt-12">
          <div className="flex items-end justify-between gap-4">
            <h2 id="tools-title" className="font-display text-4xl">أدوات تقرأ المرجع</h2>
            <Link href="/tools" className="text-library">كل الأدوات</Link>
          </div>
          <ul className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {featuredTools.map((tool) => (
              <li key={tool.code}>
                <article className="h-full rounded-3xl border border-line bg-raised p-5">
                  <h3 className="font-display text-2xl">{tool.name}</h3>
                  <p className="mt-2 text-sm leading-7 text-muted">{tool.description}</p>
                  <Link href={tool.route} className="mt-4 inline-block text-library">استخدام الأداة</Link>
                </article>
              </li>
            ))}
          </ul>
        </section>
        <section aria-labelledby="assistant-home" className="mt-12 max-w-3xl rounded-3xl border border-line bg-raised p-6">
          <h2 id="assistant-home" className="font-display text-4xl">اسأل المساعد اللغوي</h2>
          <p className="mt-3 leading-8 text-muted">سؤال واحد عن كلمة أو قاعدة أو جذر، والإجابة تعتمد على المحتوى المنشور في المرجع.</p>
          <Link href="/assistant" className="mt-4 inline-block text-library">افتح المساعد اللغوي</Link>
        </section>
      </section>
      <SectionGrid />
    </main>
  );
}
