"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { grammarAdminLinks, grammarLabel } from "@/lib/grammar";

type Rule = { id: string; title: string; status: string; version: number };
type Topic = { id: string; title: string };
type Page<T> = { items: T[] };
type Session = { permissions: string[] };

export default function AdminGrammarRulesPage() {
  const router = useRouter();
  const [rules, setRules] = useState<Rule[]>([]);
  const [topics, setTopics] = useState<Topic[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page<Rule>>("/api/v1/admin/grammar/rules?page=0&size=20"),
      adminFetch<Page<Topic>>("/api/v1/admin/grammar/topics?page=0&size=50"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]).then(([rulePage, topicPage, session]) => {
      if (cancelled) return;
      setRules(rulePage.items);
      setTopics(topicPage.items);
      setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل القواعد.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createRule(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const created = await adminFetch<Rule>("/api/v1/admin/grammar/rules", {
      method: "POST",
      body: JSON.stringify({
        topicId: String(form.get("topicId") ?? ""),
        title: String(form.get("title") ?? ""),
        summary: String(form.get("summary") ?? ""),
        ruleText: String(form.get("ruleText") ?? ""),
        displayOrder: 1,
      }),
    });
    router.push(`/admin/grammar/rules/${created.id}`);
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">قواعد النحو</h1>
      <nav aria-label="أقسام النحو" className="flex flex-wrap gap-3">{grammarAdminLinks.map((item) => <Link key={item.href} href={item.href}>{item.label}</Link>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      {can(permissions, "grammar.rule.create") ? (
        <form onSubmit={createRule} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <select name="topicId" required className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
            <option value="">اختر الموضوع</option>
            {topics.map((topic) => <option key={topic.id} value={topic.id}>{topic.title}</option>)}
          </select>
          <input name="title" required placeholder="عنوان القاعدة" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="summary" placeholder="ملخص" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <textarea name="ruleText" placeholder="نص مختصر" className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">إنشاء القاعدة</button>
        </form>
      ) : null}
      <ul className="space-y-3">
        {rules.map((rule) => (
          <li key={rule.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <Link href={`/admin/grammar/rules/${rule.id}`} className="font-display text-3xl">{rule.title}</Link>
            <p className="text-sm text-muted">{grammarLabel(rule.status)}</p>
          </li>
        ))}
      </ul>
    </main>
  );
}
