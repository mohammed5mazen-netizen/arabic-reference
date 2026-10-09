"use client";

import { useEffect, useRef, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { licenseNeedsWarning, licenseWarning } from "@/lib/dictionary";
import { contentTypeLabel, licenseLabel, statusLabel } from "@/lib/editorial";

type Source = {
  id: string;
  title: string;
  author: string | null;
  edition: string | null;
  publisher: string | null;
  publicationYear: number | null;
  licenseType: string;
  licenseLabel: string;
  status: string;
  publishableLicense: boolean;
  citationCount: number;
  lastUsedAt: string | null;
  version: number;
};
type Page = { items: Source[]; total: number };
type Session = { permissions: string[] };
type DuplicateGroup = { identityKey: string; sources: Source[] };
type Usage = { contentType: string; id: string; title: string; status: string; href: string };

const licenses = ["PUBLIC_DOMAIN", "CC0", "CC_BY", "CC_BY_SA", "PERMISSION_GRANTED", "RESTRICTED", "UNKNOWN"];

export default function SourcesPage() {
  const [page, setPage] = useState<Page | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [license, setLicense] = useState("CC_BY");
  const [duplicates, setDuplicates] = useState<DuplicateGroup[]>([]);
  const [usage, setUsage] = useState<Usage[] | null>(null);
  const [selected, setSelected] = useState<Source | null>(null);
  const [pendingDelete, setPendingDelete] = useState<Source | null>(null);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const dialogRef = useRef<HTMLDialogElement>(null);

  async function load() {
    const [sources, session, groups] = await Promise.all([
      adminFetch<Page>("/api/v1/admin/sources?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
      adminFetch<DuplicateGroup[]>("/api/v1/admin/sources/duplicates").catch(() => []),
    ]);
    setPage(sources);
    setPermissions(session.permissions);
    setDuplicates(groups);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page>("/api/v1/admin/sources?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
      adminFetch<DuplicateGroup[]>("/api/v1/admin/sources/duplicates").catch(() => []),
    ])
      .then(([sources, session, groups]) => {
        if (cancelled) return;
        setPage(sources);
        setPermissions(session.permissions);
        setDuplicates(groups);
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
        publisher: String(form.get("publisher") ?? ""),
        edition: String(form.get("edition") ?? ""),
        publicationYear: form.get("year") ? Number(form.get("year")) : null,
        licenseType: chosen,
        publicDomain: chosen === "PUBLIC_DOMAIN" || chosen === "CC0",
        attributionText: String(form.get("attributionText") ?? ""),
      }),
    });
    event.currentTarget.reset();
    await load();
  }

  async function showUsage(source: Source) {
    setSelected(source);
    const rows = await adminFetch<Usage[]>(`/api/v1/admin/sources/${source.id}/usage`);
    setUsage(rows);
  }

  function askDelete(source: Source) {
    setPendingDelete(source);
    dialogRef.current?.showModal();
  }

  async function confirmDelete() {
    if (!pendingDelete) return;
    try {
      await adminFetch(`/api/v1/admin/sources/${pendingDelete.id}`, {
        method: "DELETE",
        body: JSON.stringify({ version: pendingDelete.version }),
      });
      setNotice("حُذف المصدر.");
    } catch (caught: unknown) {
      setError(caught instanceof Error ? caught.message : "تعذر حذف المصدر.");
    }
    dialogRef.current?.close();
    setPendingDelete(null);
    await load();
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">المصادر</h1>
      <p className="text-muted">الفهرس يعرض البيانات المحفوظة. التكرار المحتمل لا يُدمج تلقائيًا.</p>
      {error ? <p role="alert">{error}</p> : null}
      {notice ? <p role="status">{notice}</p> : null}
      {can(permissions, "source.manage") ? (
        <form onSubmit={createSource} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <input name="title" required placeholder="عنوان المصدر" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="author" placeholder="المؤلف" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="publisher" placeholder="الناشر" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="edition" placeholder="الطبعة" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="year" inputMode="numeric" placeholder="السنة" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <select name="licenseType" value={license} onChange={(event) => setLicense(event.target.value)} className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
            {licenses.map((item) => (
              <option key={item} value={item}>{licenseLabel(item)}</option>
            ))}
          </select>
          {licenseNeedsWarning(license) ? <p role="alert">{licenseWarning}</p> : null}
          <textarea name="attributionText" required placeholder="نص الإسناد" className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">حفظ المصدر</button>
        </form>
      ) : null}
      <div className="overflow-x-auto rounded-[var(--radius)] border border-line bg-raised">
        <table className="w-full min-w-[56rem] text-right">
          <caption className="sr-only">فهرس المصادر</caption>
          <thead>
            <tr className="border-b border-line text-sm text-muted">
              <th scope="col" className="px-3 py-3">العنوان</th>
              <th scope="col" className="px-3 py-3">المؤلف</th>
              <th scope="col" className="px-3 py-3">الطبعة</th>
              <th scope="col" className="px-3 py-3">الناشر</th>
              <th scope="col" className="px-3 py-3">السنة</th>
              <th scope="col" className="px-3 py-3">الحقوق</th>
              <th scope="col" className="px-3 py-3">الحالة</th>
              <th scope="col" className="px-3 py-3">الاستشهادات</th>
              <th scope="col" className="px-3 py-3">آخر استخدام</th>
              <th scope="col" className="px-3 py-3">إجراء</th>
            </tr>
          </thead>
          <tbody>
            {page?.items.map((source) => (
              <tr key={source.id} className="border-b border-line">
                <td className="px-3 py-3">{source.title}</td>
                <td className="px-3 py-3">{source.author ?? "—"}</td>
                <td className="px-3 py-3">{source.edition ?? "—"}</td>
                <td className="px-3 py-3">{source.publisher ?? "—"}</td>
                <td className="px-3 py-3">{source.publicationYear ?? "—"}</td>
                <td className="px-3 py-3">{source.licenseLabel || licenseLabel(source.licenseType)}</td>
                <td className="px-3 py-3">{statusLabel(source.status)}</td>
                <td className="px-3 py-3">{source.citationCount}</td>
                <td className="px-3 py-3">{source.lastUsedAt ? <time dateTime={source.lastUsedAt}>{source.lastUsedAt}</time> : "—"}</td>
                <td className="px-3 py-3">
                  {can(permissions, "source.usage.view") ? (
                    <button type="button" className="min-h-10 underline" onClick={() => void showUsage(source)}>مستخدم في</button>
                  ) : null}
                  {can(permissions, "source.manage") ? (
                    <button type="button" className="ms-3 min-h-10 underline" onClick={() => askDelete(source)}>حذف</button>
                  ) : null}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <section className="space-y-3">
        <h2 className="font-display text-3xl">مصادر محتملة التكرار</h2>
        {duplicates.length === 0 ? <p>لا توجد مجموعات تكرار محتملة.</p> : null}
        {duplicates.map((group) => (
          <article key={group.identityKey} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <h3 className="font-medium">{group.identityKey}</h3>
            <ul>
              {group.sources.map((source) => (
                <li key={source.id}>{source.title} · {statusLabel(source.status)}</li>
              ))}
            </ul>
          </article>
        ))}
      </section>
      {selected && usage ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">مستخدم في: {selected.title}</h2>
          {usage.length === 0 ? <p>لا يستخدم هذا المصدر في مواد حالية.</p> : null}
          <ul>
            {usage.map((item) => (
              <li key={`${item.contentType}-${item.id}`}>
                <a className="underline" href={item.href}>{contentTypeLabel(item.contentType)} · {item.title} · {statusLabel(item.status)}</a>
              </li>
            ))}
          </ul>
        </section>
      ) : null}
      <dialog ref={dialogRef} className="rounded-[1.5rem] border border-line bg-raised p-6 text-ink">
        <h2 className="font-display text-3xl">حذف المصدر</h2>
        <p className="mt-3">الحذف متاح للمسودة غير المستخدمة. المصدر المستخدم أو المنشور يبقى.</p>
        <div className="mt-4 flex gap-3">
          <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => dialogRef.current?.close()}>إلغاء</button>
          <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-[var(--paper)]" onClick={() => void confirmDelete()}>تأكيد الحذف</button>
        </div>
      </dialog>
    </main>
  );
}
