"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";

type AdminAi = {
  enabled: boolean;
  provider: string;
  model: string;
  keyConfigured: boolean;
  promptVersion: string;
  requests: number;
  grounded: number;
  partial: number;
  insufficient: number;
  providerErrors: number;
  averageLatencyMs: number;
};

export default function AdminAiPage() {
  const [status, setStatus] = useState<AdminAi | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<AdminAi>("/api/v1/admin/ai").then((next) => {
      if (!cancelled) setStatus(next);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح حالة المساعد.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main className="mx-auto w-full max-w-5xl px-5 py-8">
      <h1 className="font-display text-4xl">المساعد اللغوي</h1>
      <p className="mt-3 leading-7 text-muted">حالة المساعد والأعداد المجمّعة. لا يُعرض السؤال ولا مفتاح المزود.</p>
      {error ? <p role="alert" className="mt-4">{error}</p> : null}
      {status ? (
        <dl className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div><dt>التفعيل</dt><dd>{status.enabled ? "مفعّل" : "غير متاح"}</dd></div>
          <div><dt>المزود</dt><dd>{status.provider}</dd></div>
          <div><dt>النموذج</dt><dd><bdi>{status.model}</bdi></dd></div>
          <div><dt>المفتاح مضبوط</dt><dd>{status.keyConfigured ? "نعم" : "لا"}</dd></div>
          <div><dt>إصدار التوجيه</dt><dd><bdi>{status.promptVersion}</bdi></dd></div>
          <div><dt>الطلبات</dt><dd>{status.requests}</dd></div>
          <div><dt>موثّقة</dt><dd>{status.grounded}</dd></div>
          <div><dt>جزئية</dt><dd>{status.partial}</dd></div>
          <div><dt>غير كافية</dt><dd>{status.insufficient}</dd></div>
          <div><dt>أعطال المزود</dt><dd>{status.providerErrors}</dd></div>
          <div><dt>متوسط الزمن</dt><dd>{Math.round(status.averageLatencyMs)} ms</dd></div>
        </dl>
      ) : null}
    </main>
  );
}
