"use client";

import { useEffect, useState } from "react";
import { EditorialQueue } from "@/components/editorial-queue";
import { adminFetch } from "@/lib/admin-api";
import { citationFinding, rightsFinding, severityLabel, type QualityFinding, type QueuePage } from "@/lib/editorial";

type Findings = { items: QualityFinding[] };

export default function PublishingInboxPage() {
  const [queue, setQueue] = useState<QueuePage | null>(null);
  const [findings, setFindings] = useState<QualityFinding[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<QueuePage>("/api/v1/admin/editorial/publishing?page=0&size=20"),
      adminFetch<Findings>("/api/v1/admin/editorial/quality/findings?severity=BLOCKER&page=0&size=50").catch(() => ({ items: [] })),
    ])
      .then(([nextQueue, nextFindings]) => {
        if (cancelled) return;
        setQueue(nextQueue);
        setFindings(nextFindings.items);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل صندوق النشر.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const rights = findings.filter((item) => rightsFinding(item.code));
  const citations = findings.filter((item) => citationFinding(item.code));
  const other = findings.filter((item) => !rightsFinding(item.code) && !citationFinding(item.code));

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">صندوق النشر</h1>
      <p className="text-muted">المواد المتحققة، ثم موانع الحقوق والاستشهاد والجودة المفتوحة.</p>
      {error ? <p role="alert">{error}</p> : null}
      <section className="space-y-3">
        <h2 className="font-display text-3xl">تم التحقق</h2>
        <EditorialQueue items={queue?.items ?? []} emptyTitle={queue ? "لا توجد مواد متحققة بانتظار النشر." : "جاري التحميل"} />
      </section>
      <FindingGroup title="موانع الحقوق" items={rights} />
      <FindingGroup title="موانع الاستشهاد" items={citations} />
      <FindingGroup title="موانع النشر الأخرى" items={other} />
    </main>
  );
}

function FindingGroup({ title, items }: { title: string; items: QualityFinding[] }) {
  return (
    <section className="space-y-3">
      <h2 className="font-display text-3xl">{title}</h2>
      {items.length === 0 ? <p className="text-muted">لا توجد عناصر في هذه المجموعة.</p> : null}
      <ul className="space-y-2">
        {items.map((item) => (
          <li key={item.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <p>{severityLabel(item.severity)} · {item.message}</p>
          </li>
        ))}
      </ul>
    </section>
  );
}
