"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";

type Entry = { id: string; lemmaOriginal: string; status: string; version: number };
type Page = { items: Entry[]; total: number };
type Session = { permissions: string[] };

export default function ReviewQueuePage() {
  const [page, setPage] = useState<Page | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  async function load() {
    const [queue, session] = await Promise.all([
      adminFetch<Page>("/api/v1/admin/dictionary/review?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]);
    setPage(queue);
    setPermissions(session.permissions);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page>("/api/v1/admin/dictionary/review?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([queue, session]) => {
        if (cancelled) return;
        setPage(queue);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل المراجعات.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function act(entry: Entry, action: "verify" | "return") {
    await adminFetch(`/api/v1/admin/dictionary/entries/${entry.id}/${action}`, {
      method: "POST",
      body: JSON.stringify(action === "return" ? { version: entry.version, reason } : { version: entry.version }),
    });
    await load();
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">قائمة المراجعة</h1>
      {error ? <p role="alert">{error}</p> : null}
      <ul className="space-y-3">
        {page?.items.length === 0 ? <li className="text-muted">لا توجد مداخل قيد المراجعة.</li> : null}
        {page?.items.map((entry) => (
          <li key={entry.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <p className="font-display text-3xl">{entry.lemmaOriginal}</p>
            <p className="text-sm text-muted">{entry.status}</p>
            {can(permissions, "dictionary.entry.review") ? (
              <div className="mt-3 grid gap-2">
                <button type="button" onClick={() => void act(entry, "verify")} className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">اعتماد</button>
                <input value={reason} onChange={(event) => setReason(event.target.value)} placeholder="سبب الإعادة" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
                <button type="button" onClick={() => void act(entry, "return")} className="min-h-12 rounded-2xl border border-line">إعادة مع سبب</button>
              </div>
            ) : null}
          </li>
        ))}
      </ul>
    </main>
  );
}
