"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { literatureAdminSections } from "@/lib/knowledge";
import { KnowledgeReview } from "@/components/knowledge-review";

type Session = { permissions: string[] };
type Created = { id: string; version: number };

const unknownDate = { precision: "UNKNOWN", calendar: "UNSPECIFIED", circa: false };

export default function AdminLiteraturePage() {
  const [permissions, setPermissions] = useState<string[]>([]);
  const [name, setName] = useState("");
  const [workId, setWorkId] = useState("");
  const [version, setVersion] = useState(0);
  const [rights, setRights] = useState("UNKNOWN");
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<Session>("/api/v1/admin/auth/session").then((session) => {
      if (!cancelled) setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح الأدب.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  function fail(caught: unknown) {
    setError(caught instanceof Error ? caught.message : "تعذر الحفظ.");
  }

  return (
    <main id="content" className="space-y-8">
      <h1 className="font-display text-5xl">الأدب</h1>
      <nav aria-label="أقسام الأدب" className="flex flex-wrap gap-3">{literatureAdminSections.map((section) => <span key={section}>{section}</span>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      {can(permissions, "literature.figure.manage") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الحقبات والأعلام والأنواع</h2>
          <input value={name} onChange={(event) => setName(event.target.value)} placeholder="الاسم" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <div className="flex flex-wrap gap-3">
            <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void adminFetch("/api/v1/admin/literature/eras", { method: "POST", body: JSON.stringify({ name, summary: name, displayOrder: 1 }) }).catch(fail)}>حقبة</button>
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch("/api/v1/admin/literature/genres", { method: "POST", body: JSON.stringify({ name, description: name }) }).catch(fail)}>نوع</button>
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch("/api/v1/admin/literature/schools", { method: "POST", body: JSON.stringify({ name, description: name }) }).catch(fail)}>مدرسة</button>
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch("/api/v1/admin/literature/figures", { method: "POST", body: JSON.stringify({ canonicalName: name, biographySummary: name, birth: unknownDate, death: unknownDate }) }).catch(fail)}>علم</button>
          </div>
        </section>
      ) : null}
      {can(permissions, "literature.work.manage") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الأعمال</h2>
          <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void adminFetch<Created>("/api/v1/admin/literature/works", { method: "POST", body: JSON.stringify({ title: name, description: name, languageCode: "ar" }) }).then((created) => { setWorkId(created.id); setVersion(created.version); }).catch(fail)}>إنشاء عمل</button>
        </section>
      ) : null}
      {can(permissions, "literature.rights.manage") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الحقوق</h2>
          <input value={workId} onChange={(event) => setWorkId(event.target.value)} placeholder="معرّف العمل" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <select value={rights} onChange={(event) => setRights(event.target.value)} className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
            <option value="UNKNOWN">غير محسومة</option>
            <option value="PUBLIC_DOMAIN">ملكية عامة</option>
            <option value="LICENSED">مرخّص</option>
            <option value="RESTRICTED">مقيّد</option>
          </select>
          <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<Created>(`/api/v1/admin/literature/works/${workId}/rights`, { method: "PATCH", body: JSON.stringify({ version, rights, rightsNote: "قرار تحريري" }) }).then((created) => setVersion(created.version)).catch(fail)}>حفظ الحقوق</button>
        </section>
      ) : null}
      <KnowledgeReview endpoint="/api/v1/admin/literature/review" paths={{ ERA: "/api/v1/admin/literature/eras", GENRE: "/api/v1/admin/literature/genres", SCHOOL: "/api/v1/admin/literature/schools", FIGURE: "/api/v1/admin/literature/figures", WORK: "/api/v1/admin/literature/works" }} editPermission="literature.figure.manage" reviewPermission="literature.review" publishPermission="literature.publish" />
    </main>
  );
}
