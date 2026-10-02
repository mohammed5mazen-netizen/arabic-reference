import { SearchPanel } from "@/components/search-panel";
import { SectionGrid } from "@/components/section-grid";
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
      </section>
      <SectionGrid />
    </main>
  );
}
