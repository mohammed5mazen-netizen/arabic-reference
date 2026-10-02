"use client";

import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";

type Root = { id: string; rootOriginal: string; radicalCount: number; status: string; version: number };
type Page = { items: Root[]; total: number };
type Session = { permissions: string[] };

export default function RootsPage() {
  const [page, setPage] = useState<Page | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");

  async function load() {
    const [roots, session] = await Promise.all([
      adminFetch<Page>("/api/v1/admin/dictionary/roots?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]);
    setPage(roots);
    setPermissions(session.permissions);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page>("/api/v1/admin/dictionary/roots?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([roots, session]) => {
        if (cancelled) return;
        setPage(roots);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل الجذور.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createRoot(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await adminFetch("/api/v1/admin/dictionary/roots", {
      method: "POST",
      body: JSON.stringify({ original: String(form.get("original") ?? ""), notes: String(form.get("notes") ?? "") || null }),
    });
    event.currentTarget.reset();
    await load();
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">الجذور</h1>
      {error ? <p role="alert">{error}</p> : null}
      {can(permissions, "dictionary.root.manage") ? (
        <form onSubmit={createRoot} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <input name="original" required placeholder="الجذر، مثل كتب" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="notes" placeholder="ملاحظة للجذور غير الثلاثية والرباعية" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">إضافة جذر</button>
        </form>
      ) : null}
      <ul className="space-y-3">
        {page?.items.map((root) => (
          <li key={root.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <p className="font-display text-3xl">{root.rootOriginal}</p>
            <p className="text-sm text-muted">{root.radicalCount} · {root.status}</p>
          </li>
        ))}
      </ul>
    </main>
  );
}
