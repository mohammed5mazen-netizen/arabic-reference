"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { EditorialQueue } from "@/components/editorial-queue";
import { adminFetch } from "@/lib/admin-api";
import { emptyReviewMessage, type EditorialDashboard, type QueuePage } from "@/lib/editorial";

const cards: { key: keyof EditorialDashboard; label: string }[] = [
  { key: "draft", label: "مسودة" },
  { key: "inReview", label: "قيد المراجعة" },
  { key: "changesRequested", label: "طُلب تعديل" },
  { key: "verified", label: "تم التحقق" },
  { key: "readyToPublish", label: "جاهز للنشر" },
  { key: "published", label: "منشور" },
  { key: "archived", label: "مؤرشف" },
  { key: "qualityIssues", label: "ملاحظات الجودة" },
];

export default function EditorialHomePage() {
  const [counts, setCounts] = useState<EditorialDashboard | null>(null);
  const [queue, setQueue] = useState<QueuePage | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<EditorialDashboard>("/api/v1/admin/editorial/dashboard"),
      adminFetch<QueuePage>("/api/v1/admin/editorial/queue?page=0&size=20&sort=updatedAt&direction=desc"),
    ])
      .then(([nextCounts, nextQueue]) => {
        if (cancelled) return;
        setCounts(nextCounts);
        setQueue(nextQueue);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل غرفة العمليات.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main id="content" className="space-y-6">
      <header>
        <h1 className="font-display text-5xl">غرفة العمليات التحريرية</h1>
        <p className="mt-3 text-muted">أرقام من قاعدة البيانات للمواد اللغوية فقط.</p>
      </header>
      {error ? <p role="alert">{error}</p> : null}
      <nav className="flex flex-wrap gap-3" aria-label="أقسام التشغيل">
        <Link className="underline" href="/admin/editorial/reviews">صندوق المراجعة</Link>
        <Link className="underline" href="/admin/editorial/publishing">صندوق النشر</Link>
        <Link className="underline" href="/admin/editorial/quality">مركز الجودة</Link>
        <Link className="underline" href="/admin/sources">المصادر</Link>
      </nav>
      <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {cards.map((card) => (
          <article key={card.key} className="rounded-[2rem] border border-line bg-raised p-5">
            <h2 className="text-sm text-muted">{card.label}</h2>
            <p className="mt-3 font-display text-4xl">{counts ? String(counts[card.key]) : "…"}</p>
          </article>
        ))}
      </section>
      <section className="space-y-3">
        <h2 className="font-display text-3xl">أحدث المواد</h2>
        {counts?.emptyReview ? <p>{emptyReviewMessage}</p> : null}
        <EditorialQueue items={queue?.items ?? []} emptyTitle={queue ? "لا توجد مواد في القائمة." : "جاري التحميل"} />
      </section>
    </main>
  );
}
