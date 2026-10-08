import Link from "next/link";
import { SearchPanel } from "@/components/search-panel";
import { SiteNav } from "@/components/site-nav";
import { ThemeToggle } from "@/components/theme-toggle";
import { siteName } from "@/lib/site";

export function SiteHeader() {
  return (
    <header className="relative z-20 border-b border-line bg-paper/90">
      <div className="mx-auto flex w-full max-w-6xl flex-wrap items-center gap-3 px-5 py-4">
        <Link href="/" className="flex min-h-11 items-center gap-3">
          <span aria-hidden="true" className="grid h-11 w-11 place-items-center rounded-2xl border border-line bg-raised text-library">
            <svg viewBox="0 0 48 48" className="h-7 w-7" fill="none">
              <path d="M10 34V14h8.5c4.8 0 7.8 2.6 7.8 6.7 0 4.2-3 6.8-7.8 6.8H16V34H10Zm6-11.2h2.2c2.2 0 3.5-1.1 3.5-2.9s-1.3-2.8-3.5-2.8H16v5.7Z" fill="currentColor" />
              <path d="M30 34V14h6v20h-6Z" fill="currentColor" />
            </svg>
          </span>
          <span className="font-display text-2xl">{siteName}</span>
        </Link>
        <div className="order-3 w-full min-w-0 lg:order-none lg:mx-4 lg:max-w-md lg:flex-1">
          <SearchPanel variant="header" />
        </div>
        <div className="ms-auto flex items-center gap-2">
          <SiteNav />
          <ThemeToggle />
        </div>
      </div>
    </header>
  );
}
