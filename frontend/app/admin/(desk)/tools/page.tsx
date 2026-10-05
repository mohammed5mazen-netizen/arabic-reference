"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";

type ToolCard = { code: string; name: string; status: string; route: string; description: string };
type ToolCount = { code: string; calls: number; emptyResults: number; errors: number };
type AdminTools = { tools: ToolCard[]; counts: ToolCount[] };

const statusLabel: Record<string, string> = {
  AVAILABLE: "متاح",
  LIMITED: "تغطية محدودة",
  COMING_SOON: "قريبًا",
};

export default function AdminToolsPage() {
  const [status, setStatus] = useState<AdminTools | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<AdminTools>("/api/v1/admin/tools").then((next) => {
      if (!cancelled) setStatus(next);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح حالة الأدوات.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="mx-auto w-full max-w-5xl px-5 py-8">
      <h1 className="font-display text-4xl">الأدوات اللغوية</h1>
      <p className="mt-3 leading-7 text-muted">حالة الأدوات والأعداد المجمّعة. لا يُعرض نص الزائر.</p>
      {error ? <p role="alert" className="mt-4">{error}</p> : null}
      <ul className="mt-6 space-y-3">
        {status?.tools.map((tool) => {
          const count = status.counts.find((item) => item.code === tool.code);
          return (
            <li key={tool.code} className="rounded-3xl border border-line bg-raised p-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <h2 className="font-display text-2xl">{tool.name}</h2>
                <span>{statusLabel[tool.status] ?? tool.status}</span>
              </div>
              <p className="mt-2 text-sm leading-7 text-muted">{tool.description}</p>
              <p className="mt-2 text-sm">
                الاستدعاءات: {count?.calls ?? 0} · بلا نتيجة: {count?.emptyResults ?? 0} · الأخطاء: {count?.errors ?? 0}
              </p>
            </li>
          );
        })}
      </ul>
    </main>
  );
}
