"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { articleAdminSections } from "@/lib/knowledge";
import { KnowledgeReview } from "@/components/knowledge-review";

type Session = { permissions: string[] };
type Created = { id: string; version: number; sections?: { id: string }[] };

export default function AdminArticlesPage() {
  const [permissions, setPermissions] = useState<string[]>([]);
  const [title, setTitle] = useState("");
  const [articleId, setArticleId] = useState("");
  const [sectionId, setSectionId] = useState("");
  const [version, setVersion] = useState(0);
  const [citationId, setCitationId] = useState("");
  const [targetId, setTargetId] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<Session>("/api/v1/admin/auth/session").then((session) => {
      if (!cancelled) setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح المقالات.");
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
      <h1 className="font-display text-5xl">المقالات</h1>
      <nav aria-label="أقسام المقالات" className="flex flex-wrap gap-3">{articleAdminSections.map((section) => <span key={section}>{section}</span>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      {can(permissions, "content.article.create") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">المقالة</h2>
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="العنوان" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void adminFetch<Created>("/api/v1/admin/articles", { method: "POST", body: JSON.stringify({ title, excerpt: title, articleType: "LINGUISTIC", editorName: "التحرير" }) }).then((created) => { setArticleId(created.id); setVersion(created.version); }).catch(fail)}>إنشاء</button>
        </section>
      ) : null}
      {can(permissions, "content.article.edit") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الأقسام والاستشهادات والربط</h2>
          <input value={articleId} onChange={(event) => setArticleId(event.target.value)} placeholder="معرّف المقالة" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <input value={citationId} onChange={(event) => setCitationId(event.target.value)} placeholder="معرّف الاستشهاد" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <input value={targetId} onChange={(event) => setTargetId(event.target.value)} placeholder="سجل مرتبط" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <div className="flex flex-wrap gap-3">
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<Created>(`/api/v1/admin/articles/${articleId}/sections`, { method: "POST", body: JSON.stringify({ version, heading: title, body: title }) }).then((created) => { setVersion(created.version); setSectionId(created.sections?.[0]?.id ?? sectionId); }).catch(fail)}>قسم</button>
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<Created>(`/api/v1/admin/articles/${articleId}/tags`, { method: "POST", body: JSON.stringify({ version, name: title }) }).then((created) => setVersion(created.version)).catch(fail)}>وسم</button>
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<Created>(`/api/v1/admin/articles/${articleId}/citations`, { method: "POST", body: JSON.stringify({ version, citationId, sectionId: sectionId || null }) }).then((created) => setVersion(created.version)).catch(fail)}>استشهاد</button>
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<Created>(`/api/v1/admin/articles/${articleId}/relations`, { method: "POST", body: JSON.stringify({ version, targetType: "SPELLING_RULE", targetId }) }).then((created) => setVersion(created.version)).catch(fail)}>ربط</button>
          </div>
        </section>
      ) : null}
      <KnowledgeReview endpoint="/api/v1/admin/articles/review" paths={{ ARTICLE: "/api/v1/admin/articles" }} editPermission="content.article.edit" reviewPermission="content.article.review" publishPermission="content.article.publish" />
    </main>
  );
}
