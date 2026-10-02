import type { Metadata } from "next";
import Link from "next/link";
import { LoginForm } from "@/components/admin/login-form";
import { ThemeToggle } from "@/components/theme-toggle";

export const metadata: Metadata = {
  title: "دخول الإدارة",
  robots: { index: false, follow: false },
};

export default function AdminLoginPage() {
  return (
    <main id="content" className="relative z-10 mx-auto flex min-h-screen w-full max-w-lg flex-col justify-center px-5 py-16">
      <div className="mb-6 flex items-center justify-between">
        <Link href="/" className="font-display text-2xl">
          المرجع العربي
        </Link>
        <ThemeToggle />
      </div>
      <section className="rounded-[2rem] border border-line bg-raised p-8 shadow-[var(--shadow)]">
        <p className="text-sm text-library">مكتب التحرير</p>
        <h1 className="mt-3 font-display text-4xl">دخول الإدارة</h1>
        <p className="mt-3 leading-8 text-muted">
          هذه الصفحة لفريق التحرير والإدارة فقط. زيارة المرجع وقراءة المعرفة اللغوية تبقى مفتوحة من غير حساب.
        </p>
        <div className="mt-8">
          <LoginForm />
        </div>
      </section>
    </main>
  );
}
