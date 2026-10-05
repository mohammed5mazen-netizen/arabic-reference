import Link from "next/link";
import { siteName } from "@/lib/site";
import { ThemeToggle } from "@/components/theme-toggle";

export function SiteHeader() {
  return (
    <header className="relative z-10 mx-auto flex w-full max-w-6xl items-center justify-between gap-4 px-5 py-5">
      <a href="#top" className="flex items-center gap-3">
        <span aria-hidden="true" className="grid h-11 w-11 place-items-center rounded-2xl border border-line bg-raised text-library">
          <svg viewBox="0 0 48 48" className="h-7 w-7" fill="none">
            <path d="M10 34V14h8.5c4.8 0 7.8 2.6 7.8 6.7 0 4.2-3 6.8-7.8 6.8H16V34H10Zm6-11.2h2.2c2.2 0 3.5-1.1 3.5-2.9s-1.3-2.8-3.5-2.8H16v5.7Z" fill="currentColor" />
            <path d="M30 34V14h6v20h-6Z" fill="currentColor" />
          </svg>
        </span>
        <span className="font-display text-2xl">{siteName}</span>
      </a>
      <nav aria-label="التنقل" className="flex items-center gap-4 text-sm">
        <Link href="/">الرئيسية</Link>
        <Link href="/tools">الأدوات</Link>
        <Link href="/search">البحث</Link>
        <ThemeToggle />
      </nav>
    </header>
  );
}
