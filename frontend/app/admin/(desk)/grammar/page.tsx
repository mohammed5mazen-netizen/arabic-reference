"use client";

import Link from "next/link";
import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { grammarAdminLinks, grammarLabel } from "@/lib/grammar";

type Topic = { id: string; parentId?: string | null; title: string; slug: string; category: string; status: string; displayOrder: number; version: number };
type Page = { items: Topic[] };
type Session = { permissions: string[] };

export default function AdminGrammarTopicsPage() {
  const [topics, setTopics] = useState<Topic[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");

  async function load() {
    const [page, session] = await Promise.all([
      adminFetch<Page>("/api/v1/admin/grammar/topics?page=0&size=50"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]);
    setTopics(page.items);
    setPermissions(session.permissions);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page>("/api/v1/admin/grammar/topics?page=0&size=50"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]).then(([page, session]) => {
      if (cancelled) return;
      setTopics(page.items);
      setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل النحو.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createTopic(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const parent = String(form.get("parentId") ?? "");
    await adminFetch("/api/v1/admin/grammar/topics", {
      method: "POST",
      body: JSON.stringify({
        title: String(form.get("title") ?? ""),
        summary: String(form.get("summary") ?? ""),
        category: String(form.get("category") ?? "OTHER"),
        parentId: parent || null,
        displayOrder: Number(form.get("displayOrder") ?? 0),
      }),
    });
    event.currentTarget.reset();
    await load();
  }

  async function move(topic: Topic, parentId: string) {
    await adminFetch(`/api/v1/admin/grammar/topics/${topic.id}/parent`, {
      method: "POST",
      body: JSON.stringify({ version: topic.version, parentId: parentId || null }),
    });
    await load();
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">النحو</h1>
      <nav aria-label="أقسام النحو" className="flex flex-wrap gap-3">
        {grammarAdminLinks.map((item) => <Link key={item.href} href={item.href} className="rounded-full border border-line px-4 py-2">{item.label}</Link>)}
      </nav>
      {error ? <p role="alert">{error}</p> : null}
      {can(permissions, "grammar.topic.manage") ? (
        <form onSubmit={createTopic} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <h2 className="font-display text-3xl">موضوع جديد</h2>
          <input name="title" required placeholder="العنوان" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <textarea name="summary" placeholder="ملخص" className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
          <select name="category" className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
            <option value="MARFUAT">المرفوعات</option>
            <option value="MANSUBAT">المنصوبات</option>
            <option value="MAJRURAT">المجرورات</option>
            <option value="FOUNDATIONS">أسس النحو</option>
            <option value="OTHER">أخرى</option>
          </select>
          <select name="parentId" className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
            <option value="">دون أب</option>
            {topics.map((topic) => <option key={topic.id} value={topic.id}>{topic.title}</option>)}
          </select>
          <input name="displayOrder" type="number" defaultValue={0} className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">إضافة موضوع</button>
        </form>
      ) : null}
      <ul className="space-y-3">
        {topics.map((topic) => (
          <li key={topic.id} className="rounded-[1.5rem] border border-line bg-raised p-4" style={{ marginInlineStart: `${topicDepth(topic, topics) * 1.25}rem` }}>
            <p className="font-display text-3xl">{topic.title}</p>
            <p className="text-sm text-muted">{grammarLabel(topic.category)} · {grammarLabel(topic.status)} · الترتيب {topic.displayOrder}</p>
            {can(permissions, "grammar.topic.manage") ? (
              <label className="mt-3 block text-sm">
                نقل إلى
                <select defaultValue={topic.parentId ?? ""} onChange={(event) => void move(topic, event.target.value)} className="mt-1 min-h-11 w-full rounded-2xl border border-line bg-transparent px-3">
                  <option value="">دون أب</option>
                  {topics.filter((item) => item.id !== topic.id).map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}
                </select>
              </label>
            ) : null}
          </li>
        ))}
      </ul>
    </main>
  );
}

function topicDepth(topic: Topic, topics: Topic[]): number {
  let depth = 0;
  let parentId = topic.parentId;
  const seen = new Set<string>([topic.id]);
  while (parentId && !seen.has(parentId) && depth < 12) {
    seen.add(parentId);
    depth += 1;
    parentId = topics.find((item) => item.id === parentId)?.parentId;
  }
  return depth;
}
