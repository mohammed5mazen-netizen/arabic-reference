"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { knowledgeWorkflowActions } from "@/lib/knowledge";

type Item = { kind: string; id: string; title: string; status: string; version: number };
type Session = { permissions: string[] };

export function KnowledgeReview({
  endpoint,
  paths,
  editPermission,
  reviewPermission,
  publishPermission,
}: {
  endpoint: string;
  paths: Record<string, string>;
  editPermission: string;
  reviewPermission: string;
  publishPermission: string;
}) {
  const [items, setItems] = useState<Item[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  function load() {
    return Promise.all([
      adminFetch<Item[]>(endpoint),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]).then(([queue, session]) => {
      setItems(queue);
      setPermissions(session.permissions);
    });
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Item[]>(endpoint),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]).then(([queue, session]) => {
      if (cancelled) return;
      setItems(queue);
      setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل المراجعات.");
    });
    return () => {
      cancelled = true;
    };
  }, [endpoint]);

  async function act(item: Item, action: "submit" | "verify" | "request-changes" | "publish" | "archive") {
    const collection = paths[item.kind];
    await adminFetch(`${collection}/${item.id}/${action}`, {
      method: "POST",
      body: JSON.stringify(action === "request-changes" ? { version: item.version, reason } : { version: item.version }),
    });
    await load();
  }

  return (
    <section id="review" className="space-y-3">
      <h2 className="font-display text-3xl">المراجعات</h2>
      {error ? <p role="alert">{error}</p> : null}
      <ul className="space-y-3">
        {items.length === 0 ? <li>لا توجد مواد قيد المراجعة.</li> : null}
        {items.map((item) => {
          const actions = knowledgeWorkflowActions(item.status, permissions, editPermission, reviewPermission, publishPermission);
          return (
            <li key={`${item.kind}-${item.id}`} className="rounded-[1.5rem] border border-line bg-raised p-4">
              <p className="text-sm text-muted">{item.kind}</p>
              <p className="font-display text-3xl">{item.title}</p>
              {actions.includes("verify") ? <button type="button" className="mt-3 min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void act(item, "verify")}>اعتماد</button> : null}
              {actions.includes("publish") ? <button type="button" className="mt-3 min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void act(item, "publish")}>نشر</button> : null}
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
    </section>
  );
}
