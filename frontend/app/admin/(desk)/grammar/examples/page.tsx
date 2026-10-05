"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { grammarAdminLinks, grammarLabel } from "@/lib/grammar";

type Example = { id: string; ruleId: string; ruleTitle?: string | null; textOriginal: string; exampleType: string };
type Page = { items: Example[] };

export default function AdminGrammarExamplesPage() {
  const [examples, setExamples] = useState<Example[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<Page>("/api/v1/admin/grammar/examples?page=0&size=20")
      .then((page) => {
        if (!cancelled) setExamples(page.items);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل الأمثلة.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">أمثلة النحو</h1>
      <nav aria-label="أقسام النحو" className="flex flex-wrap gap-3">{grammarAdminLinks.map((item) => <Link key={item.href} href={item.href}>{item.label}</Link>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      <ul className="space-y-3">
        {examples.length === 0 ? <li>لا توجد أمثلة بعد.</li> : null}
        {examples.map((example) => (
          <li key={example.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <p className="font-display text-2xl">{example.textOriginal}</p>
            <p className="text-sm text-muted">{grammarLabel(example.exampleType)}</p>
            <Link href={`/admin/grammar/rules/${example.ruleId}`}>{example.ruleTitle || "القاعدة"}</Link>
          </li>
        ))}
      </ul>
    </main>
  );
}
