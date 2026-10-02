"use client";

import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { morphologyLabel } from "@/lib/morphology";

type Pattern = { id: string; code: string; patternOriginal: string; category: string; radicalCount: number; description?: string | null; status: string; version: number };
type Analysis = { id: string; lemma?: string | null; patternOriginal?: string | null; status: string; verbClass?: string | null; version: number };
type Rule = { code: string; description: string; enabled: boolean; ruleSetVersion: string; version: number };
type Coverage = { ruleSetVersion: string; rows: { feature: string; support: string; note: string }[] };
type Page<T> = { items: T[]; total: number };
type Session = { permissions: string[] };

export default function AdminMorphologyPage() {
  const [patterns, setPatterns] = useState<Pattern[]>([]);
  const [analyses, setAnalyses] = useState<Analysis[]>([]);
  const [rules, setRules] = useState<Rule[]>([]);
  const [coverage, setCoverage] = useState<Coverage | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");

  async function load() {
    const [patternPage, analysisPage, ruleList, coverageView, session] = await Promise.all([
      adminFetch<Page<Pattern>>("/api/v1/admin/morphology/patterns?page=0&size=20"),
      adminFetch<Page<Analysis>>("/api/v1/admin/morphology/analyses?page=0&size=20"),
      adminFetch<Rule[]>("/api/v1/admin/morphology/rules").catch(() => [] as Rule[]),
      adminFetch<Coverage>("/api/v1/admin/morphology/coverage"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]);
    setPatterns(patternPage.items);
    setAnalyses(analysisPage.items);
    setRules(ruleList);
    setCoverage(coverageView);
    setPermissions(session.permissions);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page<Pattern>>("/api/v1/admin/morphology/patterns?page=0&size=20"),
      adminFetch<Page<Analysis>>("/api/v1/admin/morphology/analyses?page=0&size=20"),
      adminFetch<Rule[]>("/api/v1/admin/morphology/rules").catch(() => [] as Rule[]),
      adminFetch<Coverage>("/api/v1/admin/morphology/coverage"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([patternPage, analysisPage, ruleList, coverageView, session]) => {
        if (cancelled) return;
        setPatterns(patternPage.items);
        setAnalyses(analysisPage.items);
        setRules(ruleList);
        setCoverage(coverageView);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل الصرف.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createPattern(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await adminFetch("/api/v1/admin/morphology/patterns", {
      method: "POST",
      body: JSON.stringify({
        code: String(form.get("code") ?? ""),
        patternOriginal: String(form.get("patternOriginal") ?? ""),
        category: String(form.get("category") ?? "OTHER"),
        radicalCount: Number(form.get("radicalCount") ?? 3),
        description: String(form.get("description") ?? ""),
      }),
    });
    event.currentTarget.reset();
    await load();
  }

  async function togglePattern(pattern: Pattern) {
    const action = pattern.status === "ACTIVE" ? "deactivate" : "activate";
    await adminFetch(`/api/v1/admin/morphology/patterns/${pattern.id}/${action}`, {
      method: "POST",
      body: JSON.stringify({ version: pattern.version }),
    });
    await load();
  }

  async function toggleRule(rule: Rule) {
    await adminFetch(`/api/v1/admin/morphology/rules/${rule.code}`, {
      method: "POST",
      body: JSON.stringify({ enabled: !rule.enabled, version: rule.version }),
    });
    await load();
  }

  return (
    <main id="content" className="space-y-8">
      <h1 className="font-display text-5xl">الصرف</h1>
      {error ? <p role="alert">{error}</p> : null}
      <section aria-labelledby="patterns-title" className="space-y-3">
        <h2 id="patterns-title" className="font-display text-3xl">الأوزان</h2>
        {can(permissions, "morphology.pattern.manage") ? (
          <form onSubmit={createPattern} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
            <input name="code" required placeholder="رمز ثابت مثل FA3ALA" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
            <input name="patternOriginal" required placeholder="الوزن العربي" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
            <input name="radicalCount" required type="number" min={2} max={5} defaultValue={3} className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
            <select name="category" className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
              <option value="VERB">فعل</option>
              <option value="NOUN">اسم</option>
              <option value="OTHER">أخرى</option>
            </select>
            <input name="description" placeholder="وصف عربي" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
            <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">إضافة وزن</button>
          </form>
        ) : null}
        <div className="space-y-3">
          {patterns.map((pattern) => (
            <article key={pattern.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
              <p className="font-display text-3xl">{pattern.patternOriginal}</p>
              <p className="text-sm text-muted">{pattern.code} · {morphologyLabel(pattern.category)} · {pattern.radicalCount} أحرف · {pattern.status === "ACTIVE" ? "مفعّل" : "متوقف"}</p>
              {pattern.description ? <p className="mt-2">{pattern.description}</p> : null}
              {can(permissions, "morphology.pattern.manage") ? (
                <button type="button" className="mt-3 min-h-11 rounded-2xl border border-line px-4" onClick={() => void togglePattern(pattern)}>
                  {pattern.status === "ACTIVE" ? "إيقاف" : "تفعيل"}
                </button>
              ) : null}
            </article>
          ))}
        </div>
      </section>
      <section aria-labelledby="analyses-title" className="space-y-3">
        <h2 id="analyses-title" className="font-display text-3xl">التحليلات الموثقة</h2>
        {analyses.length === 0 ? <p className="text-muted">لا توجد تحليلات يدوية بعد.</p> : null}
        <div className="space-y-3">
          {analyses.map((analysis) => (
            <article key={analysis.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
              <p className="text-lg">{analysis.lemma}</p>
              <p className="text-sm text-muted">{[analysis.patternOriginal, morphologyLabel(analysis.verbClass), morphologyLabel(analysis.status)].filter(Boolean).join(" · ")}</p>
            </article>
          ))}
        </div>
      </section>
      <section aria-labelledby="rules-title" className="space-y-3">
        <h2 id="rules-title" className="font-display text-3xl">القواعد</h2>
        <p className="text-sm text-muted">يمكن تفعيل القاعدة أو إيقافها. نص القاعدة البرمجي لا يُعدَّل من هنا.</p>
        <div className="space-y-3">
          {rules.map((rule) => (
            <article key={rule.code} className="rounded-[1.5rem] border border-line bg-raised p-4">
              <p>{rule.description}</p>
              <p className="text-sm text-muted">{rule.code} · {rule.enabled ? "مفعّلة" : "متوقفة"} · {rule.ruleSetVersion}</p>
              {can(permissions, "morphology.rule.manage") ? (
                <button type="button" className="mt-3 min-h-11 rounded-2xl border border-line px-4" onClick={() => void toggleRule(rule)}>
                  {rule.enabled ? "إيقاف" : "تفعيل"}
                </button>
              ) : null}
            </article>
          ))}
        </div>
      </section>
      <section aria-labelledby="coverage-title" className="space-y-3">
        <h2 id="coverage-title" className="font-display text-3xl">تغطية التصريف</h2>
        {coverage ? <p className="text-sm text-muted">إصدار القواعد: {coverage.ruleSetVersion}</p> : null}
        <div className="space-y-3">
          {coverage?.rows.map((row) => (
            <article key={row.feature} className="rounded-[1.5rem] border border-line bg-raised p-4">
              <p>{morphologyLabel(row.support)}</p>
              <p className="mt-1 text-sm leading-7">{row.note}</p>
            </article>
          ))}
        </div>
      </section>
    </main>
  );
}
