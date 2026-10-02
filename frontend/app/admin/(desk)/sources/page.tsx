"use client";

import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { licenseNeedsWarning, licenseWarning } from "@/lib/dictionary";

type Source = { id: string; title: string; licenseType: string; status: string; publishableLicense: boolean; version: number };
type Page = { items: Source[]; total: number };
type Session = { permissions: string[] };

const licenses = ["PUBLIC_DOMAIN", "CC0", "CC_BY", "CC_BY_SA", "PERMISSION_GRANTED", "RESTRICTED", "UNKNOWN"];

export default function SourcesPage() {
  const [page, setPage] = useState<Page | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [license, setLicense] = useState("CC_BY");
  const [error, setError] = useState("");

  async function load() {
    const [sources, session] = await Promise.all([
      adminFetch<Page>("/api/v1/admin/sources?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]);
    setPage(sources);
    setPermissions(session.permissions);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page>("/api/v1/admin/sources?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([sources, session]) => {
        if (cancelled) return;
        setPage(sources);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل المصادر.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createSource(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const chosen = String(form.get("licenseType") ?? "UNKNOWN");
    await adminFetch("/api/v1/admin/sources", {
      method: "POST",
      body: JSON.stringify({
        sourceType: "DICTIONARY",
        title: String(form.get("title") ?? ""),
        author: String(form.get("author") ?? ""),
        edition: String(form.get("edition") ?? ""),
        licenseType: chosen,
        publicDomain: chosen === "PUBLIC_DOMAIN" || chosen === "CC0",
        attributionText: String(form.get("attributionText") ?? ""),
      }),
    });
    event.currentTarget.reset();
    await load();
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">المصادر</h1>
      {error ? <p role="alert">{error}</p> : null}
      {can(permissions, "source.manage") ? (
        <form onSubmit={createSource} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <input name="title" required placeholder="عنوان المصدر" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="author" placeholder="المؤلف" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="edition" placeholder="الطبعة" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <select name="licenseType" value={license} onChange={(event) => setLicense(event.target.value)} className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
            {licenses.map((item) => (
              <option key={item}>{item}</option>
            ))}
          </select>
          {licenseNeedsWarning(license) ? <p role="alert">{licenseWarning}</p> : null}
          <textarea name="attributionText" required placeholder="نص الإسناد" className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">حفظ المصدر</button>
        </form>
      ) : null}
      <ul className="space-y-3">
        {page?.items.map((source) => (
          <li key={source.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <p className="font-medium">{source.title}</p>
            <p className="text-sm text-muted">{source.licenseType} · {source.status}</p>
            {licenseNeedsWarning(source.licenseType) ? <p className="text-sm">{licenseWarning}</p> : null}
          </li>
        ))}
      </ul>
    </main>
  );
}
