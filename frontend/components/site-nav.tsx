"use client";

import { useRef } from "react";
import Link from "next/link";
import { navigationGroups } from "@/lib/navigation";

export function SiteNav() {
  const dialog = useRef<HTMLDialogElement>(null);
  return (
    <>
      <nav aria-label="أقسام المرجع" className="hidden items-center gap-2 lg:flex">
        {navigationGroups.map((group) => (
          <details key={group.id} className="relative">
            <summary className="min-h-11 cursor-pointer list-none rounded-2xl px-3 py-2">{group.title}</summary>
            <ul className="absolute z-20 mt-2 min-w-44 rounded-2xl border border-line bg-raised p-2 shadow-[var(--shadow)]">
              {group.links.map((link) => (
                <li key={link.href}>
                  <Link href={link.href} className="block min-h-11 rounded-xl px-3 py-2">{link.label}</Link>
                </li>
              ))}
            </ul>
          </details>
        ))}
      </nav>
      <button type="button" className="min-h-11 rounded-2xl border border-line px-3 lg:hidden" onClick={() => dialog.current?.showModal()}>
        القائمة
      </button>
      <dialog ref={dialog} aria-label="التنقل" className="max-h-[80vh] overflow-auto">
        <div className="mb-4 flex items-center justify-between">
          <p className="font-display text-2xl">أقسام المرجع</p>
          <button type="button" className="min-h-11 rounded-2xl border border-line px-3" onClick={() => dialog.current?.close()}>إغلاق</button>
        </div>
        {navigationGroups.map((group) => (
          <section key={group.id} className="mt-4">
            <h2 className="text-sm text-muted">{group.title}</h2>
            <ul className="mt-2 space-y-1">
              {group.links.map((link) => (
                <li key={link.href}>
                  <Link href={link.href} className="block min-h-11 rounded-xl px-2 py-2" onClick={() => dialog.current?.close()}>{link.label}</Link>
                </li>
              ))}
            </ul>
          </section>
        ))}
      </dialog>
    </>
  );
}
