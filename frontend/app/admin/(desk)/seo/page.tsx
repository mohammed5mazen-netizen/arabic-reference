"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";

type SeoStatus = {
  indexingEnabled: boolean;
  siteOrigin: string;
  counts: Record<string, number>;
  indexableCount: number;
  lastGenerated: string;
  orphanEntries: number;
  brokenRelations: number;
  invalidPaths: number;
  problems: string[];
};

export default function AdminSeoPage() {
  const [status, setStatus] = useState<SeoStatus | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    adminFetch<SeoStatus>("/api/v1/admin/seo/status")
      .then((next) => {
        if (!cancelled) setStatus(next);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل حالة الفهرسة.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const counts = Object.entries(status?.counts ?? {});

  return (
    <main className="mx-auto w-full max-w-4xl px-5 py-8">
      <h1 className="font-display text-4xl">الفهرسة</h1>
      <p className="mt-3 leading-8 text-muted">حالة الاكتشاف العام. هذه الصفحة لا تعدّل البيانات الوصفية ولا تتصل بمحرك بحث خارجي.</p>
      {error ? <p className="mt-6" role="alert">{error}</p> : null}
      {status ? (
        <>
          <dl className="mt-8 grid gap-4 sm:grid-cols-2">
            <div className="rounded-3xl border border-line bg-raised p-4">
              <dt className="text-sm text-muted">النطاق</dt>
              <dd className="mt-1 font-display text-2xl">{status.siteOrigin || "غير مضبوط"}</dd>
            </div>
            <div className="rounded-3xl border border-line bg-raised p-4">
              <dt className="text-sm text-muted">الفهرسة</dt>
              <dd className="mt-1 font-display text-2xl">{status.indexingEnabled ? "مفعّلة" : "متوقفة"}</dd>
            </div>
            <div className="rounded-3xl border border-line bg-raised p-4">
              <dt className="text-sm text-muted">عناوين قابلة للفهرسة</dt>
              <dd className="mt-1 font-display text-2xl">{status.indexableCount}</dd>
            </div>
            <div className="rounded-3xl border border-line bg-raised p-4">
              <dt className="text-sm text-muted">آخر قراءة للخريطة</dt>
              <dd className="mt-1 font-display text-2xl">{status.lastGenerated || "لم تُقرأ بعد"}</dd>
            </div>
          </dl>
          <table className="mt-8 w-full border-collapse text-start">
            <caption className="mb-3 text-start font-display text-2xl">التعداد حسب النوع</caption>
            <thead>
              <tr>
                <th scope="col" className="border-b border-line px-3 py-2 text-start">النوع</th>
                <th scope="col" className="border-b border-line px-3 py-2 text-start">العدد</th>
              </tr>
            </thead>
            <tbody>
              {counts.length === 0 ? (
                <tr>
                  <td colSpan={2} className="px-3 py-4 text-muted">لا توجد صفحات منشورة في الفهرس.</td>
                </tr>
              ) : counts.map(([type, count]) => (
                <tr key={type}>
                  <th scope="row" className="border-b border-line px-3 py-2 text-start font-normal">{type}</th>
                  <td className="border-b border-line px-3 py-2">{count}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <ul className="mt-8 space-y-2 leading-8">
            <li>مداخل منشورة بلا جذر وبلا علاقة: {status.orphanEntries}</li>
            <li>علاقات منشورة مكسورة: {status.brokenRelations}</li>
            <li>مسارات غير صالحة في الفهرس: {status.invalidPaths}</li>
          </ul>
          {status.problems.length > 0 ? (
            <section className="mt-8" aria-labelledby="seo-problems">
              <h2 id="seo-problems" className="font-display text-3xl">ملاحظات</h2>
              <ul className="mt-3 list-disc space-y-2 pe-5 leading-8">
                {status.problems.map((problem) => <li key={problem}>{problem}</li>)}
              </ul>
            </section>
          ) : null}
        </>
      ) : null}
    </main>
  );
}
