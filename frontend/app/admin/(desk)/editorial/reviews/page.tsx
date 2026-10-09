"use client";

import { useEffect, useState } from "react";
import { EditorialQueue } from "@/components/editorial-queue";
import { adminFetch } from "@/lib/admin-api";
import { emptyReviewMessage, type QueuePage } from "@/lib/editorial";

const sections = [
  { id: "waiting", label: "بانتظار المراجعة" },
  { id: "assigned", label: "مكلّف إليّ" },
  { id: "changes", label: "طُلب تعديل" },
  { id: "recent", label: "رُوجع حديثًا" },
];

export default function ReviewInboxPage() {
  const [section, setSection] = useState("waiting");
  const [page, setPage] = useState<QueuePage | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<QueuePage>(`/api/v1/admin/editorial/reviews?section=${section}&page=0&size=20`)
      .then((next) => {
        if (!cancelled) setPage(next);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل صندوق المراجعة.");
      });
    return () => {
      cancelled = true;
    };
  }, [section]);

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">صندوق المراجعة</h1>
      {error ? <p role="alert">{error}</p> : null}
      <div className="flex flex-wrap gap-2" role="tablist" aria-label="أقسام المراجعة">
        {sections.map((item) => (
          <button
            key={item.id}
            type="button"
            role="tab"
            aria-selected={section === item.id}
            className="min-h-12 rounded-2xl border border-line px-4"
            onClick={() => setSection(item.id)}
          >
            {item.label}
          </button>
        ))}
      </div>
      {section === "waiting" && page?.total === 0 ? <p>{emptyReviewMessage}</p> : null}
      <EditorialQueue items={page?.items ?? []} emptyTitle={page ? "لا توجد مواد في هذا القسم." : "جاري التحميل"} />
    </main>
  );
}
