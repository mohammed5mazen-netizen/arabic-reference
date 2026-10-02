import Link from "next/link";
import { SiteFooter } from "@/components/site-footer";
import { SiteHeader } from "@/components/site-header";

export default function NotFound() {
  return (
    <>
      <SiteHeader />
      <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-24">
      <p className="text-sm text-library">404</p>
      <h1 className="mt-3 font-display text-5xl">الصفحة غير موجودة</h1>
      <p className="mt-4 text-lg leading-8 text-muted">عد إلى الصفحة الرئيسية وتابع القراءة. المرجع لا يطلب تسجيل دخول.</p>
      <Link href="/" className="mt-8 inline-flex min-h-12 items-center rounded-full bg-library px-6 font-medium text-[var(--paper)]">
        الصفحة الرئيسية
      </Link>
    </main>
      <SiteFooter />
    </>
  );
}
