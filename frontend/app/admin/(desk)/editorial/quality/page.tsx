"use client";

import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { contentTypeLabel, severityLabel, type QualityFinding } from "@/lib/editorial";

type Findings = { items: QualityFinding[] };
type Session = { permissions: string[] };

export default function QualityCenterPage() {
  const [findings, setFindings] = useState<QualityFinding[]>([]);
  const [severity, setSeverity] = useState("");
  const [permissions, setPermissions] = useState<string[]>([]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function load(nextSeverity = severity) {
    const query = nextSeverity ? `?severity=${nextSeverity}&page=0&size=50` : "?page=0&size=50";
    const page = await adminFetch<Findings>(`/api/v1/admin/editorial/quality/findings${query}`);
    setFindings(page.items);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Findings>("/api/v1/admin/editorial/quality/findings?page=0&size=50"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([page, session]) => {
        if (cancelled) return;
        setFindings(page.items);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل الجودة.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function scan(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const scope = String(form.get("scope") ?? "PUBLISHED");
    const contentType = String(form.get("contentType") ?? "");
    const contentId = String(form.get("contentId") ?? "");
    const result = await adminFetch<{ records: number; findings: number }>("/api/v1/admin/editorial/quality/scans", {
      method: "POST",
      body: JSON.stringify({
        scope,
        contentType: contentType || null,
        contentId: contentId || null,
      }),
    });
    setMessage(`اكتمل الفحص: ${result.records} سجلًا و${result.findings} ملاحظة.`);
    await load();
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">مركز الجودة</h1>
      <p className="text-muted">قواعد حتمية. لا يراجع المساعد اللغوي هذه المواد.</p>
      {error ? <p role="alert">{error}</p> : null}
      {message ? <p role="status">{message}</p> : null}
      <div className="flex flex-wrap gap-2">
        {[
          ["", "الكل"],
          ["BLOCKER", "مانع للنشر"],
          ["WARNING", "تحذير"],
          ["INFO", "ملاحظة"],
        ].map(([value, label]) => (
          <button
            key={label}
            type="button"
            className="min-h-12 rounded-2xl border border-line px-4"
            aria-pressed={severity === value}
            onClick={() => {
              setSeverity(value);
              void load(value).catch((caught: unknown) => setError(caught instanceof Error ? caught.message : "تعذر التصفية."));
            }}
          >
            {label}
          </button>
        ))}
      </div>
      {can(permissions, "editorial.quality.run") ? (
        <form onSubmit={scan} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <label>
            النطاق
            <select name="scope" className="mt-2 block min-h-12 w-full rounded-2xl border border-line bg-transparent px-4">
              <option value="PUBLISHED">المنشور</option>
              <option value="TYPE">نوع</option>
              <option value="RECORD">سجل واحد</option>
            </select>
          </label>
          <label>
            النوع
            <input name="contentType" className="mt-2 block min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" placeholder="DICTIONARY_ENTRY" />
          </label>
          <label>
            المعرّف
            <input name="contentId" className="mt-2 block min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          </label>
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">تشغيل الفحص</button>
        </form>
      ) : null}
      <div className="overflow-x-auto rounded-[var(--radius)] border border-line bg-raised">
        <table className="w-full min-w-[40rem] text-right">
          <caption className="sr-only">نتائج الجودة</caption>
          <thead>
            <tr className="border-b border-line text-sm text-muted">
              <th scope="col" className="px-4 py-3">الشدة</th>
              <th scope="col" className="px-4 py-3">الرمز</th>
              <th scope="col" className="px-4 py-3">النوع</th>
              <th scope="col" className="px-4 py-3">الرسالة</th>
            </tr>
          </thead>
          <tbody>
            {findings.map((item) => (
              <tr key={item.id} className="border-b border-line">
                <td className="px-4 py-3">{severityLabel(item.severity)}</td>
                <td className="px-4 py-3">{item.code}</td>
                <td className="px-4 py-3">{contentTypeLabel(item.contentType)}</td>
                <td className="px-4 py-3">{item.message}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {findings.length === 0 ? <p>لا توجد ملاحظات مفتوحة في هذا التصفية.</p> : null}
    </main>
  );
}
