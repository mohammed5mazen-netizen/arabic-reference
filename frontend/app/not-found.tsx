import Link from "next/link";
import { SiteFooter } from "@/components/site-footer";
import { SiteHeader } from "@/components/site-header";
import { LinkButton } from "@/components/ui/button";
import { knowledgeAreas } from "@/lib/navigation";

export default function NotFound() {
  return (
    <>
      <SiteHeader />
      <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-16">
        <p className="text-sm text-library">404</p>
        <h1 className="mt-3 font-display text-5xl">الصفحة غير موجودة</h1>
        <p className="mt-4 text-lg leading-8 text-muted">العنوان غير متاح. عد إلى الرئيسية أو ابحث في المرجع. القراءة لا تتطلب حسابًا.</p>
        <div className="mt-8 flex flex-wrap gap-3">
          <LinkButton href="/" size="large">الصفحة الرئيسية</LinkButton>
          <LinkButton href="/search" variant="secondary" size="large">البحث</LinkButton>
        </div>
        <ul className="mt-8 space-y-2">
          {knowledgeAreas.map((area) => (
            <li key={area.href}><Link href={area.href} className="inline-flex min-h-11 items-center text-library">{area.label}</Link></li>
          ))}
        </ul>
      </main>
      <SiteFooter />
    </>
  );
}
