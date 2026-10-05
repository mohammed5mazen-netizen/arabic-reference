"use client";

import Link from "next/link";
import { useEffect, useState, type FormEvent } from "react";
import { useParams } from "next/navigation";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { grammarLabel, grammarWorkflowActions, ruleEditorSections } from "@/lib/grammar";

type Component = { id: string; type: string; heading?: string | null; body: string };
type Example = { id: string; textOriginal: string; exampleType: string };
type Relation = { id: string; type: string; targetRuleId: string };
type Rule = {
  id: string;
  title: string;
  summary?: string | null;
  ruleText?: string | null;
  status: string;
  version: number;
  components: Component[];
  examples: Example[];
  relations: Relation[];
  citationIds: string[];
};
type Session = { permissions: string[] };

export default function GrammarRuleEditorPage() {
  const params = useParams<{ id: string }>();
  const [rule, setRule] = useState<Rule | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [section, setSection] = useState<(typeof ruleEditorSections)[number]["id"]>("basics");
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  async function load() {
    const [current, session] = await Promise.all([
      adminFetch<Rule>(`/api/v1/admin/grammar/rules/${params.id}`),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]);
    setRule(current);
    setPermissions(session.permissions);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Rule>(`/api/v1/admin/grammar/rules/${params.id}`),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]).then(([current, session]) => {
      if (cancelled) return;
      setRule(current);
      setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح القاعدة.");
    });
    return () => {
      cancelled = true;
    };
  }, [params.id]);

  async function saveBasics(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!rule) return;
    const form = new FormData(event.currentTarget);
    await adminFetch(`/api/v1/admin/grammar/rules/${rule.id}`, {
      method: "PATCH",
      body: JSON.stringify({
        version: rule.version,
        title: String(form.get("title") ?? ""),
        summary: String(form.get("summary") ?? ""),
        ruleText: String(form.get("ruleText") ?? ""),
        displayOrder: 1,
      }),
    });
    await load();
  }

  async function addComponent(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!rule) return;
    const form = new FormData(event.currentTarget);
    await adminFetch(`/api/v1/admin/grammar/rules/${rule.id}/components`, {
      method: "POST",
      body: JSON.stringify({ version: rule.version, type: String(form.get("type") ?? "NOTE"), heading: String(form.get("heading") ?? ""), body: String(form.get("body") ?? "") }),
    });
    event.currentTarget.reset();
    await load();
  }

  async function addExample(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!rule) return;
    const form = new FormData(event.currentTarget);
    const citation = String(form.get("citationId") ?? "");
    await adminFetch(`/api/v1/admin/grammar/rules/${rule.id}/examples`, {
      method: "POST",
      body: JSON.stringify({
        version: rule.version,
        textOriginal: String(form.get("textOriginal") ?? ""),
        explanation: String(form.get("explanation") ?? ""),
        exampleType: String(form.get("exampleType") ?? "CONSTRUCTED"),
        citationId: citation || null,
      }),
    });
    event.currentTarget.reset();
    await load();
  }

  async function addCitation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!rule) return;
    const form = new FormData(event.currentTarget);
    await adminFetch(`/api/v1/admin/grammar/rules/${rule.id}/citations`, {
      method: "POST",
      body: JSON.stringify({ version: rule.version, citationId: String(form.get("citationId") ?? "") }),
    });
    event.currentTarget.reset();
    await load();
  }

  async function act(action: "submit" | "verify" | "request-changes" | "publish" | "archive") {
    if (!rule) return;
    await adminFetch(`/api/v1/admin/grammar/rules/${rule.id}/${action}`, {
      method: "POST",
      body: JSON.stringify(action === "request-changes" ? { version: rule.version, reason } : { version: rule.version }),
    });
    await load();
  }

  const actions = rule ? grammarWorkflowActions(rule.status, permissions) : [];

  return (
    <main id="content" className="space-y-6">
      <p><Link href="/admin/grammar/rules">القواعد</Link></p>
      <h1 className="font-display text-5xl">{rule?.title ?? "القاعدة"}</h1>
      {error ? <p role="alert">{error}</p> : null}
      <div role="tablist" aria-label="أقسام المحرر" className="flex flex-wrap gap-2">
        {ruleEditorSections.map((item) => (
          <button key={item.id} type="button" role="tab" id={`tab-${item.id}`} aria-selected={section === item.id} aria-controls={`panel-${item.id}`} className="min-h-11 rounded-full border border-line px-4" onClick={() => setSection(item.id)}>
            {item.label}
          </button>
        ))}
      </div>
      {rule && section === "basics" ? (
        <form id="panel-basics" role="tabpanel" aria-labelledby="tab-basics" onSubmit={saveBasics} className="grid gap-3">
          <input name="title" defaultValue={rule.title} className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <textarea name="summary" defaultValue={rule.summary ?? ""} className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
          {can(permissions, "grammar.rule.edit") ? <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">حفظ</button> : null}
        </form>
      ) : null}
      {rule && section === "statement" ? (
        <form id="panel-statement" role="tabpanel" aria-labelledby="tab-statement" onSubmit={saveBasics} className="grid gap-3">
          <textarea name="ruleText" defaultValue={rule.ruleText ?? ""} className="min-h-32 rounded-2xl border border-line bg-transparent px-4 py-3" />
          <input type="hidden" name="title" value={rule.title} />
          <input type="hidden" name="summary" value={rule.summary ?? ""} />
          {can(permissions, "grammar.rule.edit") ? <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">حفظ النص</button> : null}
        </form>
      ) : null}
      {rule && section === "conditions" ? (
        <section id="panel-conditions" role="tabpanel" aria-labelledby="tab-conditions" className="space-y-3">
          {rule.components.map((component) => <article key={component.id} className="rounded-2xl border border-line p-4"><p className="text-sm text-muted">{grammarLabel(component.type)}</p><p>{component.body}</p></article>)}
          {can(permissions, "grammar.rule.edit") ? (
            <form onSubmit={addComponent} className="grid gap-3">
              <select name="type" className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
                <option value="DEFINITION">التعريف</option>
                <option value="CORE_RULE">القاعدة</option>
                <option value="CONDITION">الشرط</option>
                <option value="EXCEPTION">الاستثناء</option>
                <option value="NOTE">ملاحظة</option>
              </select>
              <input name="heading" placeholder="عنوان اختياري" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
              <textarea name="body" required placeholder="النص" className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
              <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">إضافة جزء</button>
            </form>
          ) : null}
        </section>
      ) : null}
      {rule && section === "examples" ? (
        <section id="panel-examples" role="tabpanel" aria-labelledby="tab-examples" className="space-y-3">
          {rule.examples.map((example) => <article key={example.id} className="rounded-2xl border border-line p-4"><p>{example.textOriginal}</p><p className="text-sm text-muted">{grammarLabel(example.exampleType)}</p></article>)}
          {can(permissions, "grammar.example.manage") ? (
            <form onSubmit={addExample} className="grid gap-3">
              <select name="exampleType" className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
                <option value="CONSTRUCTED">مثال تحريري</option>
                <option value="QUOTED">مثال مقتبس</option>
                <option value="COUNTEREXAMPLE">مثال مقابل</option>
                <option value="QURANIC">شاهد قرآني</option>
                <option value="POETRY">شاهد شعري</option>
              </select>
              <textarea name="textOriginal" required placeholder="نص المثال" className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
              <input name="explanation" placeholder="شرح" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
              <input name="citationId" placeholder="معرّف الاستشهاد، مطلوب للمقتبس" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
              <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">إضافة مثال</button>
            </form>
          ) : null}
        </section>
      ) : null}
      {rule && section === "relations" ? (
        <section id="panel-relations" role="tabpanel" aria-labelledby="tab-relations" className="space-y-2">
          {rule.relations.map((relation) => <p key={relation.id}>{grammarLabel(relation.type)}</p>)}
          {rule.relations.length === 0 ? <p>لا توجد علاقات بعد.</p> : null}
        </section>
      ) : null}
      {rule && section === "sources" ? (
        <section id="panel-sources" role="tabpanel" aria-labelledby="tab-sources" className="space-y-3">
          <p>{rule.citationIds.length} مصدرًا مرتبطًا</p>
          {can(permissions, "citation.manage") ? (
            <form onSubmit={addCitation} className="grid gap-3">
              <input name="citationId" required placeholder="معرّف الاستشهاد" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
              <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">ربط مصدر</button>
            </form>
          ) : null}
        </section>
      ) : null}
      {rule && section === "review" ? (
        <section id="panel-review" role="tabpanel" aria-labelledby="tab-review" className="space-y-3">
          <p>{grammarLabel(rule.status)}</p>
          {actions.includes("request-changes") ? <input value={reason} onChange={(event) => setReason(event.target.value)} placeholder="سبب الإعادة" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" /> : null}
          {actions.map((action) => (
            <button key={action} type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void act(action)}>
              {action === "submit" ? "إرسال للمراجعة" : action === "verify" ? "اعتماد" : action === "request-changes" ? "طلب تعديل" : action === "publish" ? "نشر" : "أرشفة"}
            </button>
          ))}
        </section>
      ) : null}
    </main>
  );
}
