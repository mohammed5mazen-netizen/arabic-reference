"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { rebuildConfirmation } from "@/lib/search";

type Status = {
  indexVersion: number;
  documentCount: number;
  lastRebuildAt?: string | null;
  counts: Record<string, number>;
  consistent: boolean;
  missingDocuments: number;
  staleDocuments: number;
  outdatedVersions: number;
};
type Session = { permissions: string[] };

export default function AdminSearchPage() {
  const [status, setStatus] = useState<Status | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Status>("/api/v1/admin/search/status"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]).then(([nextStatus, session]) => {
      if (cancelled) return;
      setStatus(nextStatus);
      setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح فهرس البحث.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  async function rebuild() {
    if (!window.confirm(rebuildConfirmation())) return;
    setBusy(true);
    setError("");
    try {
      const next = await adminFetch<Status>("/api/v1/admin/search/reindex", { method: "POST" });
      setStatus(next);
    } catch (caught: unknown) {
      setError(caught instanceof Error ? caught.message : "تعذر إعادة البناء.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">فهرس البحث</h1>
      {error ? <p role="alert">{error}</p> : null}
      {status ? (
        <section className="grid gap-4 rounded-[1.5rem] border border-line bg-raised p-5 sm:grid-cols-2">
          <p>إصدار الفهرس: {status.indexVersion}</p>
          <p>الوثائق المفهرسة: {status.documentCount}</p>
          <p>آخر إعادة بناء: {status.lastRebuildAt ?? "لم تُبنَ بعد"}</p>
          <p>الاتساق: {status.consistent ? "سليم" : `ناقص ${status.missingDocuments} / زائد ${status.staleDocuments}`}</p>
          <ul className="sm:col-span-2">
            {Object.entries(status.counts).map(([type, count]) => <li key={type}>{type}: {count}</li>)}
          </ul>
        </section>
      ) : null}
      {can(permissions, "search.reindex") ? (
        <button type="button" onClick={rebuild} disabled={busy} className="min-h-12 rounded-2xl bg-library px-6 text-[var(--paper)]">
          إعادة بناء الفهرس
        </button>
      ) : null}
    </main>
  );
}
