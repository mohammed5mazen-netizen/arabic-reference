"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { grammarAdminLinks, grammarLabel, grammarWorkflowActions } from "@/lib/grammar";

type Item = { kind: string; id: string; title: string; status: string; version: number };
type Page = { items: Item[] };
type Session = { permissions: string[] };

const paths: Record<string, string> = { TOPIC: "topics", RULE: "rules", CONCEPT: "concepts", ANNOTATION: "annotations" };

export default function AdminGrammarReviewPage() {
  const [items, setItems] = useState<Item[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  async function load() {
    const [page, session] = await Promise.all([
      adminFetch<Page>("/api/v1/admin/grammar/review?status=IN_REVIEW&page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]);
    setItems(page.items);
    setPermissions(session.permissions);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page>("/api/v1/admin/grammar/review?status=IN_REVIEW&page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]).then(([page, session]) => {
      if (cancelled) return;
      setItems(page.items);
      setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل المراجعات.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  async function act(item: Item, action: "verify" | "request-changes") {
    const collection = paths[item.kind];
    await adminFetch(`/api/v1/admin/grammar/${collection}/${item.id}/${action}`, {
      method: "POST",
      body: JSON.stringify(action === "request-changes" ? { version: item.version, reason } : { version: item.version }),
    });
    await load();
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">مراجعات النحو</h1>
      <nav aria-label="أقسام النحو" className="flex flex-wrap gap-3">{grammarAdminLinks.map((item) => <Link key={item.href} href={item.href}>{item.label}</Link>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      <ul className="space-y-3">
        {items.length === 0 ? <li>لا توجد مواد قيد المراجعة.</li> : null}
        {items.map((item) => {
          const actions = grammarWorkflowActions(item.status, permissions);
          return (
            <li key={`${item.kind}-${item.id}`} className="rounded-[1.5rem] border border-line bg-raised p-4">
              <p className="text-sm text-muted">{grammarLabel(item.kind)}</p>
              <p className="font-display text-3xl">{item.title}</p>
              {actions.includes("verify") ? <button type="button" className="mt-3 min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void act(item, "verify")}>اعتماد</button> : null}
              {actions.includes("request-changes") ? (
                <div className="mt-3 grid gap-2">
                  <input value={reason} onChange={(event) => setReason(event.target.value)} placeholder="سبب الإعادة" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
                  <button type="button" className="min-h-12 rounded-2xl border border-line" onClick={() => void act(item, "request-changes")}>طلب تعديل</button>
                </div>
              ) : null}
            </li>
          );
        })}
      </ul>
    </main>
  );
}
