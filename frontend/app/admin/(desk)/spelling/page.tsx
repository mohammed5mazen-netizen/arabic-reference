"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { spellingAdminSections } from "@/lib/knowledge";
import { KnowledgeReview } from "@/components/knowledge-review";

type Session = { permissions: string[] };
type Created = { id: string; version: number; title?: string };

export default function AdminSpellingPage() {
  const [permissions, setPermissions] = useState<string[]>([]);
  const [title, setTitle] = useState("");
  const [summary, setSummary] = useState("");
  const [topicId, setTopicId] = useState("");
  const [ruleId, setRuleId] = useState("");
  const [version, setVersion] = useState(0);
  const [citationId, setCitationId] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<Session>("/api/v1/admin/auth/session").then((session) => {
      if (!cancelled) setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح الإملاء.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createTopic() {
    const created = await adminFetch<Created>("/api/v1/admin/spelling/topics", {
      method: "POST",
      body: JSON.stringify({ title, summary, displayOrder: 1 }),
    });
    setTopicId(created.id);
    setMessage(`موضوع: ${created.id}`);
  }

  async function createRule() {
    const created = await adminFetch<Created>("/api/v1/admin/spelling/rules", {
      method: "POST",
      body: JSON.stringify({ topicId, title, summary, coreRule: summary || title, difficulty: "BEGINNER" }),
    });
    setRuleId(created.id);
    setVersion(created.version);
    setMessage(`قاعدة: ${created.id}`);
  }

  async function addClause() {
    const created = await adminFetch<Created>(`/api/v1/admin/spelling/rules/${ruleId}/clauses`, {
      method: "POST",
      body: JSON.stringify({ version, kind: "DEFINITION", heading: title, body: summary || title }),
    });
    setVersion(created.version);
  }

  async function addExample() {
    const created = await adminFetch<Created>(`/api/v1/admin/spelling/rules/${ruleId}/examples`, {
      method: "POST",
      body: JSON.stringify({ version, kind: "CONSTRUCTED", correctForm: title, explanation: summary || title }),
    });
    setVersion(created.version);
  }

  async function cite() {
    const created = await adminFetch<Created>(`/api/v1/admin/spelling/rules/${ruleId}/citations`, {
      method: "POST",
      body: JSON.stringify({ version, citationId }),
    });
    setVersion(created.version);
  }

  return (
    <main id="content" className="space-y-8">
      <h1 className="font-display text-5xl">الإملاء</h1>
      <nav aria-label="أقسام الإملاء" className="flex flex-wrap gap-3">{spellingAdminSections.map((section) => <span key={section}>{section}</span>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      {message ? <p>{message}</p> : null}
      {can(permissions, "spelling.topic.manage") ? (
        <section id="topics" className="space-y-3">
          <h2 className="font-display text-3xl">الموضوعات</h2>
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="العنوان" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <input value={summary} onChange={(event) => setSummary(event.target.value)} placeholder="الملخص" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void createTopic().catch((caught: unknown) => setError(caught instanceof Error ? caught.message : "تعذر الحفظ."))}>إنشاء موضوع</button>
        </section>
      ) : null}
      {can(permissions, "spelling.rule.create") ? (
        <section id="rules" className="space-y-3">
          <h2 className="font-display text-3xl">القواعد</h2>
          <input value={topicId} onChange={(event) => setTopicId(event.target.value)} placeholder="معرّف الموضوع" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void createRule().catch((caught: unknown) => setError(caught instanceof Error ? caught.message : "تعذر الحفظ."))}>إنشاء قاعدة</button>
        </section>
      ) : null}
      {can(permissions, "spelling.rule.edit") ? (
        <section id="examples" className="space-y-3">
          <h2 className="font-display text-3xl">الأمثلة</h2>
          <input value={ruleId} onChange={(event) => setRuleId(event.target.value)} placeholder="معرّف القاعدة" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <input value={citationId} onChange={(event) => setCitationId(event.target.value)} placeholder="معرّف الاستشهاد" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <div className="flex flex-wrap gap-3">
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void addClause().catch((caught: unknown) => setError(caught instanceof Error ? caught.message : "تعذر الحفظ."))}>إضافة شرط</button>
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void addExample().catch((caught: unknown) => setError(caught instanceof Error ? caught.message : "تعذر الحفظ."))}>إضافة مثال</button>
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void cite().catch((caught: unknown) => setError(caught instanceof Error ? caught.message : "تعذر الحفظ."))}>ربط مصدر</button>
          </div>
        </section>
      ) : null}
      <KnowledgeReview endpoint="/api/v1/admin/spelling/review" paths={{ TOPIC: "/api/v1/admin/spelling/topics", RULE: "/api/v1/admin/spelling/rules" }} editPermission="spelling.rule.edit" reviewPermission="spelling.rule.review" publishPermission="spelling.rule.publish" />
    </main>
  );
}
