"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { partOfSpeechLabel } from "@/lib/dictionary";

type Entry = { id: string; lemmaOriginal: string; partOfSpeech: string; status: string; slug: string };
type Page = { items: Entry[]; total: number };
type Session = { permissions: string[] };

const parts = ["NOUN", "VERB", "ADJECTIVE", "ADVERB", "PRONOUN", "PREPOSITION", "CONJUNCTION", "PARTICLE", "INTERJECTION", "PROPER_NOUN", "OTHER"];

export default function DictionaryPage() {
  const router = useRouter();
  const [page, setPage] = useState<Page | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page>("/api/v1/admin/dictionary/entries?page=0&size=20"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([entries, session]) => {
        if (cancelled) return;
        setPage(entries);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل المداخل.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createEntry(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const created = await adminFetch<Entry>("/api/v1/admin/dictionary/entries", {
      method: "POST",
      body: JSON.stringify({
        lemma: String(form.get("lemma") ?? ""),
        vocalizedForm: String(form.get("vocalizedForm") ?? "") || null,
        partOfSpeech: String(form.get("partOfSpeech") ?? "NOUN"),
      }),
    });
    router.push(`/admin/dictionary/${created.id}`);
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">المداخل</h1>
      {error ? <p role="alert">{error}</p> : null}
      {can(permissions, "dictionary.entry.create") ? (
        <form onSubmit={createEntry} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5 sm:grid-cols-2">
          <input name="lemma" required placeholder="الكلمة" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="vocalizedForm" placeholder="الشكل المشكول" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <select name="partOfSpeech" className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
            {parts.map((part) => (
              <option key={part} value={part}>
                {partOfSpeechLabel(part)}
              </option>
            ))}
          </select>
          <button type="submit" className="min-h-12 rounded-2xl bg-library px-4 text-[var(--paper)]">
            مدخل جديد
          </button>
        </form>
      ) : null}
      <ul className="space-y-3">
        {page?.items.map((entry) => (
          <li key={entry.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <Link href={`/admin/dictionary/${entry.id}`} className="font-display text-3xl">
              {entry.lemmaOriginal}
            </Link>
            <p className="text-sm text-muted">
              {partOfSpeechLabel(entry.partOfSpeech)} · {entry.status}
            </p>
          </li>
        ))}
      </ul>
    </main>
  );
}
