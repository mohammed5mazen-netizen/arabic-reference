"use client";

import Link from "next/link";
import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { annotationEditorFields, grammarAdminLinks, grammarLabel, grammarWorkflowActions } from "@/lib/grammar";

type Role = { code: string; labelAr: string };
type Token = { surface: string; position: string; roleCode: string; grammaticalState: string; explanation: string };
type Annotation = { id: string; sentence: string; status: string; version: number; tokens: { surface: string; position: number; roleCode?: string | null; grammaticalState?: string | null; explanation?: string | null }[] };
type Session = { permissions: string[] };

const emptyToken = (): Token => ({ surface: "", position: "", roleCode: "", grammaticalState: "", explanation: "" });

export default function GrammarAnnotationEditorPage() {
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [annotation, setAnnotation] = useState<Annotation | null>(null);
  const [tokens, setTokens] = useState<Token[]>([emptyToken(), emptyToken()]);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Role[]>("/api/v1/admin/grammar/roles"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]).then(([roleList, session]) => {
      if (cancelled) return;
      setRoles(roleList);
      setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح محرر الجمل.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createSentence(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const created = await adminFetch<Annotation>("/api/v1/admin/grammar/annotations", {
      method: "POST",
      body: JSON.stringify({ sentence: String(form.get("sentence") ?? "") }),
    });
    setAnnotation(created);
  }

  function updateToken(index: number, field: keyof Token, value: string) {
    setTokens((current) => current.map((token, tokenIndex) => tokenIndex === index ? { ...token, [field]: value } : token));
  }

  async function saveTokens(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!annotation) return;
    const saved = await adminFetch<Annotation>(`/api/v1/admin/grammar/annotations/${annotation.id}/tokens`, {
      method: "POST",
      body: JSON.stringify({
        version: annotation.version,
        tokens: tokens.filter((token) => token.surface.trim()).map((token) => ({
          surface: token.surface,
          position: Number(token.position),
          roleCode: token.roleCode || null,
          grammaticalState: token.grammaticalState || null,
          explanation: token.explanation || null,
        })),
      }),
    });
    setAnnotation(saved);
  }

  async function act(action: "submit" | "verify" | "publish") {
    if (!annotation) return;
    const saved = await adminFetch<Annotation>(`/api/v1/admin/grammar/annotations/${annotation.id}/${action}`, {
      method: "POST",
      body: JSON.stringify({ version: annotation.version }),
    });
    setAnnotation(saved);
  }

  const actions = annotation ? grammarWorkflowActions(annotation.status, permissions) : [];

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">تحليل الجمل</h1>
      <nav aria-label="أقسام النحو" className="flex flex-wrap gap-3">{grammarAdminLinks.map((item) => <Link key={item.href} href={item.href}>{item.label}</Link>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      <p className="text-sm text-muted">التقسيم يدوي. لا يُنشأ إعراب آلي.</p>
      {can(permissions, "grammar.annotation.manage") ? (
        <form onSubmit={createSentence} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <label htmlFor="sentence">الجملة</label>
          <textarea id="sentence" name="sentence" required className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">حفظ الجملة</button>
        </form>
      ) : null}
      {annotation ? (
        <form onSubmit={saveTokens} className="space-y-3">
          <p>{grammarLabel(annotation.status)} · {annotation.sentence}</p>
          {tokens.map((token, index) => (
            <fieldset key={annotationEditorFields.join("-") + index} className="grid gap-2 rounded-2xl border border-line p-4">
              <legend>كلمة {index + 1}</legend>
              <input aria-label="surface" value={token.surface} onChange={(event) => updateToken(index, "surface", event.target.value)} placeholder="الكلمة" className="min-h-11 rounded-2xl border border-line bg-transparent px-3" />
              <input aria-label="position" value={token.position} onChange={(event) => updateToken(index, "position", event.target.value)} placeholder="الترتيب" inputMode="numeric" className="min-h-11 rounded-2xl border border-line bg-transparent px-3" />
              <select aria-label="role" value={token.roleCode} onChange={(event) => updateToken(index, "roleCode", event.target.value)} className="min-h-11 rounded-2xl border border-line bg-transparent px-3">
                <option value="">دون وظيفة</option>
                {roles.map((role) => <option key={role.code} value={role.code}>{role.labelAr}</option>)}
              </select>
              <select aria-label="state" value={token.grammaticalState} onChange={(event) => updateToken(index, "grammaticalState", event.target.value)} className="min-h-11 rounded-2xl border border-line bg-transparent px-3">
                <option value="">دون حالة</option>
                <option value="RAFA">مرفوع</option>
                <option value="NASB">منصوب</option>
                <option value="JARR">مجرور</option>
                <option value="JAZM">مجزوم</option>
              </select>
              <input aria-label="explanation" value={token.explanation} onChange={(event) => updateToken(index, "explanation", event.target.value)} placeholder="الشرح" className="min-h-11 rounded-2xl border border-line bg-transparent px-3" />
            </fieldset>
          ))}
          <button type="button" className="min-h-11 rounded-2xl border border-line px-4" onClick={() => setTokens((current) => [...current, emptyToken()])}>إضافة كلمة</button>
          <button type="submit" className="min-h-12 rounded-2xl bg-library px-4 text-white">حفظ التقسيم</button>
          {actions.filter((action) => action === "submit" || action === "verify" || action === "publish").map((action) => (
            <button key={action} type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void act(action)}>
              {action === "submit" ? "إرسال للمراجعة" : action === "verify" ? "اعتماد" : "نشر"}
            </button>
          ))}
        </form>
      ) : null}
    </main>
  );
}
