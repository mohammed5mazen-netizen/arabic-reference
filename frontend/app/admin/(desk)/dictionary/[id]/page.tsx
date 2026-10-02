"use client";

import { useEffect, useState, type FormEvent } from "react";
import { useParams } from "next/navigation";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";

type Sense = { id: string; definition: string; shortDefinition?: string | null; displayOrder: number; status: string };
type Entry = {
  id: string;
  lemmaOriginal: string;
  vocalizedForm?: string | null;
  partOfSpeech: string;
  status: string;
  version: number;
  senses: Sense[];
  forms: { id: string; formType: string; originalForm: string }[];
  relations: { id: string; relationType: string; targetEntryId: string; status: string }[];
};
type Session = { permissions: string[] };
type Tab = "word" | "senses" | "forms" | "examples";

export default function EntryEditorPage() {
  const params = useParams<{ id: string }>();
  const [entry, setEntry] = useState<Entry | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [tab, setTab] = useState<Tab>("word");
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Entry>(`/api/v1/admin/dictionary/entries/${params.id}`),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([loaded, session]) => {
        if (cancelled) return;
        setEntry(loaded);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح المدخل.");
      });
    return () => {
      cancelled = true;
    };
  }, [params.id]);

  async function reload() {
    setEntry(await adminFetch<Entry>(`/api/v1/admin/dictionary/entries/${params.id}`));
  }

  async function saveWord(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!entry) return;
    const form = new FormData(event.currentTarget);
    setEntry(
      await adminFetch<Entry>(`/api/v1/admin/dictionary/entries/${entry.id}`, {
        method: "PATCH",
        body: JSON.stringify({
          version: entry.version,
          draft: {
            lemma: String(form.get("lemma") ?? ""),
            vocalizedForm: String(form.get("vocalizedForm") ?? "") || null,
            partOfSpeech: String(form.get("partOfSpeech") ?? entry.partOfSpeech),
          },
        }),
      }),
    );
  }

  async function addSense(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!entry) return;
    const form = new FormData(event.currentTarget);
    setEntry(
      await adminFetch<Entry>(`/api/v1/admin/dictionary/entries/${entry.id}/senses`, {
        method: "POST",
        body: JSON.stringify({
          version: entry.version,
          sense: {
            definition: String(form.get("definition") ?? ""),
            shortDefinition: String(form.get("shortDefinition") ?? "") || null,
            displayOrder: Number(form.get("displayOrder") ?? entry.senses.length + 1),
          },
        }),
      }),
    );
    event.currentTarget.reset();
  }

  async function addForm(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!entry) return;
    const form = new FormData(event.currentTarget);
    setEntry(
      await adminFetch<Entry>(`/api/v1/admin/dictionary/entries/${entry.id}/forms`, {
        method: "POST",
        body: JSON.stringify({
          version: entry.version,
          form: { formType: String(form.get("formType") ?? "PLURAL"), originalForm: String(form.get("originalForm") ?? "") },
        }),
      }),
    );
    event.currentTarget.reset();
  }

  async function addExample(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!entry || entry.senses.length === 0) return;
    const form = new FormData(event.currentTarget);
    setEntry(
      await adminFetch<Entry>(`/api/v1/admin/dictionary/senses/${entry.senses[0].id}/examples`, {
        method: "POST",
        body: JSON.stringify({
          version: entry.version,
          example: { kind: "EDITORIAL", text: String(form.get("text") ?? ""), displayOrder: 1 },
        }),
      }),
    );
    event.currentTarget.reset();
  }

  async function transition(action: "submit" | "verify" | "publish" | "archive") {
    if (!entry) return;
    setEntry(
      await adminFetch<Entry>(`/api/v1/admin/dictionary/entries/${entry.id}/${action}`, {
        method: "POST",
        body: JSON.stringify({ version: entry.version }),
      }),
    );
  }

  if (!entry) {
    return <main id="content">{error ? <p role="alert">{error}</p> : <p>جارٍ فتح المدخل...</p>}</main>;
  }

  const tabs: { id: Tab; label: string }[] = [
    { id: "word", label: "الكلمة" },
    { id: "senses", label: "المعاني" },
    { id: "forms", label: "الأشكال" },
    { id: "examples", label: "الأمثلة" },
  ];

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">{entry.lemmaOriginal}</h1>
      <p className="text-sm text-muted">{entry.status}</p>
      <div className="flex flex-wrap gap-2">
        {tabs.map((item) => (
          <button key={item.id} type="button" onClick={() => setTab(item.id)} className="rounded-full border border-line px-4 py-2">
            {item.label}
          </button>
        ))}
      </div>
      {tab === "word" && can(permissions, "dictionary.entry.edit") ? (
        <form onSubmit={saveWord} className="grid gap-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <input name="lemma" defaultValue={entry.lemmaOriginal} className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="vocalizedForm" defaultValue={entry.vocalizedForm ?? ""} placeholder="الشكل المشكول" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <input name="partOfSpeech" defaultValue={entry.partOfSpeech} className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">حفظ الكلمة</button>
        </form>
      ) : null}
      {tab === "senses" ? (
        <section className="space-y-4">
          <ul className="space-y-2">
            {entry.senses.map((sense) => (
              <li key={sense.id} className="rounded-2xl border border-line p-4">
                {sense.displayOrder}. {sense.definition}
              </li>
            ))}
          </ul>
          {can(permissions, "dictionary.sense.manage") ? (
            <form onSubmit={addSense} className="grid gap-3">
              <textarea name="definition" required placeholder="التعريف" className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
              <input name="shortDefinition" placeholder="تعريف مختصر" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
              <input name="displayOrder" type="number" min={1} placeholder="الترتيب" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
              <button type="submit" className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">إضافة معنى</button>
            </form>
          ) : null}
        </section>
      ) : null}
      {tab === "forms" && can(permissions, "dictionary.entry.edit") ? (
        <form onSubmit={addForm} className="grid gap-3">
          <ul>
            {entry.forms.map((form) => (
              <li key={form.id}>{form.originalForm}</li>
            ))}
          </ul>
          <select name="formType" className="min-h-12 rounded-2xl border border-line bg-transparent px-4">
            {["VOCALIZED", "PLURAL", "SINGULAR", "FEMININE", "MASCULINE", "ALTERNATE"].map((type) => (
              <option key={type}>{type}</option>
            ))}
          </select>
          <input name="originalForm" required placeholder="الشكل" className="min-h-12 rounded-2xl border border-line bg-transparent px-4" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">إضافة شكل</button>
        </form>
      ) : null}
      {tab === "examples" && can(permissions, "dictionary.example.manage") ? (
        <form onSubmit={addExample} className="grid gap-3">
          <textarea name="text" required placeholder="مثال تحريري" className="min-h-24 rounded-2xl border border-line bg-transparent px-4 py-3" />
          <button type="submit" className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">إضافة مثال</button>
        </form>
      ) : null}
      <div className="flex flex-wrap gap-2">
        {can(permissions, "dictionary.entry.submit") ? <button type="button" onClick={() => void transition("submit")} className="rounded-full border border-line px-4 py-2">إرسال للمراجعة</button> : null}
        {can(permissions, "dictionary.entry.review") ? <button type="button" onClick={() => void transition("verify")} className="rounded-full border border-line px-4 py-2">اعتماد</button> : null}
        {can(permissions, "dictionary.entry.publish") ? <button type="button" onClick={() => void transition("publish")} className="rounded-full border border-line px-4 py-2">نشر</button> : null}
        {can(permissions, "dictionary.entry.archive") ? <button type="button" onClick={() => void transition("archive").then(reload)} className="rounded-full border border-line px-4 py-2">أرشفة</button> : null}
      </div>
      {error ? <p role="alert">{error}</p> : null}
    </main>
  );
}
