"use client";

import Link from "next/link";
import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { grammarAdminLinks, grammarLabel } from "@/lib/grammar";

type Concept = { id: string; term: string; status: string };
type Page = { items: Concept[] };
type Session = { permissions: string[] };

export default function AdminGrammarConceptsPage() {
  const [concepts, setConcepts] = useState<Concept[]>([]);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");

  async function load() {
    const [page, session] = await Promise.all([
      adminFetch<Page>("/api/v1/admin/grammar/concepts?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]);
    setConcepts(page.items);
    setPermissions(session.permissions);
  }

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page>("/api/v1/admin/grammar/concepts?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]).then(([page, session]) => {
      if (cancelled) return;
      setConcepts(page.items);
      setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل المصطلحات.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createConcept(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await adminFetch("/api/v1/admin/grammar/concepts", {
      method: "POST",
      body: JSON.stringify({
        term: String(form.get("term") ?? ""),
        shortDefinition: String(form.get("shortDefinition") ?? ""),
        detailedDefinition: String(form.get("detailedDefinition") ?? ""),
      }),
    });
    event.currentTarget.reset();
    await load();
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">المصطلحات النحوية</h1>
      <nav aria-label="أقسام النحو" className="flex flex-wrap gap-3">{grammarAdminLinks.map((item) => <Link key={item.href} href={item.href}>{item.label}</Link>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      {can(permissions, "grammar.concept.manage") ? (
        <form onSubmit={createConcept} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <input name="term" required placeholder="المصطلح" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="shortDefinition" placeholder="تعريف قصير" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <textarea name="detailedDefinition" placeholder="شرح تفصيلي" className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-white">إضافة مصطلح</button>
        </form>
      ) : null}
      <ul className="space-y-3">
        {concepts.map((concept) => <li key={concept.id} className="rounded-[1.5rem] border border-line bg-raised p-4"><p className="font-display text-3xl">{concept.term}</p><p className="text-sm text-muted">{grammarLabel(concept.status)}</p></li>)}
      </ul>
    </main>
  );
}
