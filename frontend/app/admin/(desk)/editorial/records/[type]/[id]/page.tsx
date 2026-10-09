"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useCallback, useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { contentTypeLabel, statusLabel } from "@/lib/editorial";

type TimelineEvent = { kind: string; at: string; actor: string | null; detail: Record<string, string> };
type Comment = { id: string; type: string; body: string; status: string; version: number };
type DiffChange = { code: string; label: string; field: string; textDiff: { op: string; text: string }[] };
type DiffView = { fromRevision: number; toRevision: number; changes: DiffChange[] };
type Readiness = { state: string; reasons: string[]; checklist: { label: string; done: boolean }[] };
type QueuePage = { items: { title: string; status: string; version: number; href: string; contentType: string; id: string }[] };
type Session = { permissions: string[] };

export default function EditorialRecordPage() {
  const params = useParams<{ type: string; id: string }>();
  const type = params.type;
  const id = params.id;
  const [title, setTitle] = useState("");
  const [status, setStatus] = useState("");
  const [version, setVersion] = useState(0);
  const [href, setHref] = useState("");
  const [timeline, setTimeline] = useState<TimelineEvent[]>([]);
  const [comments, setComments] = useState<Comment[]>([]);
  const [diff, setDiff] = useState<DiffView | null>(null);
  const [readiness, setReadiness] = useState<Readiness | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const reload = useCallback(async () => {
    const [queue, events, notes, currentDiff, ready, session] = await Promise.all([
      adminFetch<QueuePage>(`/api/v1/admin/editorial/queue?type=${type}&q=${id}&page=0&size=5`),
      adminFetch<TimelineEvent[]>(`/api/v1/admin/editorial/records/${type}/${id}/timeline`),
      adminFetch<Comment[]>(`/api/v1/admin/editorial/records/${type}/${id}/comments?page=0&size=20`),
      adminFetch<DiffView>(`/api/v1/admin/editorial/records/${type}/${id}/diff`).catch(() => null),
      adminFetch<Readiness>(`/api/v1/admin/editorial/records/${type}/${id}/readiness`),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ]);
    const match = queue.items.find((item) => item.id === id);
    setTitle(match?.title ?? id);
    setStatus(match?.status ?? "");
    setVersion(match?.version ?? 0);
    setHref(match?.href ?? "");
    setTimeline(events);
    setComments(notes);
    setDiff(currentDiff);
    setReadiness(ready);
    setPermissions(session.permissions);
  }, [id, type]);

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<QueuePage>(`/api/v1/admin/editorial/queue?type=${type}&q=${id}&page=0&size=5`),
      adminFetch<TimelineEvent[]>(`/api/v1/admin/editorial/records/${type}/${id}/timeline`),
      adminFetch<Comment[]>(`/api/v1/admin/editorial/records/${type}/${id}/comments?page=0&size=20`),
      adminFetch<DiffView>(`/api/v1/admin/editorial/records/${type}/${id}/diff`).catch(() => null),
      adminFetch<Readiness>(`/api/v1/admin/editorial/records/${type}/${id}/readiness`),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([queue, events, notes, currentDiff, ready, session]) => {
        if (cancelled) return;
        const match = queue.items.find((item) => item.id === id);
        setTitle(match?.title ?? id);
        setStatus(match?.status ?? "");
        setVersion(match?.version ?? 0);
        setHref(match?.href ?? "");
        setTimeline(events);
        setComments(notes);
        setDiff(currentDiff);
        setReadiness(ready);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح السجل.");
      });
    return () => {
      cancelled = true;
    };
  }, [id, type]);

  async function addComment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await adminFetch(`/api/v1/admin/editorial/records/${type}/${id}/comments`, {
      method: "POST",
      body: JSON.stringify({
        kind: String(form.get("kind") ?? "GENERAL"),
        body: String(form.get("body") ?? ""),
        expectedVersion: version,
      }),
    });
    setNotice("أُضيف التعليق الداخلي.");
    event.currentTarget.reset();
    await reload();
  }

  async function resolve(comment: Comment) {
    await adminFetch(`/api/v1/admin/editorial/comments/${comment.id}/resolve`, {
      method: "POST",
      body: JSON.stringify({ version: comment.version }),
    });
    setNotice("أُغلق التعليق.");
    await reload();
  }

  async function compareRevisions(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const fromRevision = String(form.get("fromRevision") ?? "");
    const toRevision = String(form.get("toRevision") ?? "");
    const next = await adminFetch<DiffView>(
      `/api/v1/admin/editorial/records/${type}/${id}/diff?fromRevision=${fromRevision}&toRevision=${toRevision}`,
    );
    setDiff(next);
  }

  return (
    <main id="content" className="space-y-6">
      <header>
        <p className="text-sm text-muted">{contentTypeLabel(type)}</p>
        <h1 className="font-display text-5xl">{title}</h1>
        <p className="mt-2">{status ? statusLabel(status) : ""}</p>
        {href ? <Link className="underline" href={href}>فتح في لوحة المجال</Link> : null}
      </header>
      {error ? <p role="alert">{error}</p> : null}
      {notice ? <p role="status">{notice}</p> : null}
      <section className="space-y-3">
        <h2 className="font-display text-3xl">جاهزية النشر</h2>
        <p>{readiness?.state === "READY" ? "جاهز" : "موقوف"}</p>
        <ul>
          {readiness?.checklist.map((item) => (
            <li key={item.label}>{item.done ? "✓" : "✗"} {item.label}</li>
          ))}
        </ul>
        <ul>
          {readiness?.reasons.map((reason) => <li key={reason}>{reason}</li>)}
        </ul>
      </section>
      <section className="space-y-3">
        <h2 className="font-display text-3xl">الخط الزمني</h2>
        <ol className="space-y-2">
          {timeline.map((event, index) => (
            <li key={`${event.kind}-${event.at}-${index}`} className="rounded-[1.5rem] border border-line bg-raised p-4">
              <p>{event.kind}</p>
              <p className="text-sm text-muted">{event.actor ?? "النظام"} · <time dateTime={event.at}>{event.at}</time></p>
            </li>
          ))}
        </ol>
      </section>
      <section className="space-y-3">
        <h2 className="font-display text-3xl">التعليقات الداخلية</h2>
        <ul className="space-y-2">
          {comments.map((comment) => (
            <li key={comment.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
              <p>{comment.type} · {comment.status === "OPEN" ? "مفتوح" : "مغلق"}</p>
              <p>{comment.body}</p>
              {comment.status === "OPEN" && can(permissions, "editorial.comment.resolve") ? (
                <button type="button" className="mt-3 min-h-12 rounded-2xl border border-line px-4" onClick={() => void resolve(comment)}>
                  إغلاق التعليق
                </button>
              ) : null}
            </li>
          ))}
        </ul>
        {can(permissions, "editorial.comment.create") ? (
          <form onSubmit={addComment} className="grid gap-3">
            <label>
              نوع التعليق
              <select name="kind" className="mt-2 block min-h-12 w-full rounded-2xl border border-line bg-transparent px-4">
                <option value="GENERAL">عام</option>
                <option value="CORRECTION">تصحيح</option>
                <option value="SOURCE_REQUIRED">مصدر مطلوب</option>
                <option value="RIGHTS_ISSUE">مسألة حقوق</option>
                <option value="STRUCTURE">بنية</option>
                <option value="LANGUAGE">لغة</option>
                <option value="OTHER">أخرى</option>
              </select>
            </label>
            <label>
              التعليق
              <textarea name="body" required className="mt-2 block min-h-24 w-full rounded-2xl border border-line bg-transparent px-4 py-3" />
            </label>
            <button type="submit" className="min-h-12 rounded-2xl bg-library text-[var(--paper)]">إضافة تعليق</button>
          </form>
        ) : null}
      </section>
      {diff ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الفرق</h2>
          <p>من المراجعة {diff.fromRevision} إلى المراجعة {diff.toRevision}</p>
          {can(permissions, "editorial.diff.view") ? (
            <form onSubmit={compareRevisions} className="flex flex-wrap gap-3">
              <label>
                من
                <input name="fromRevision" inputMode="numeric" className="ms-2 min-h-12 rounded-2xl border border-line bg-transparent px-3" />
              </label>
              <label>
                إلى
                <input name="toRevision" inputMode="numeric" className="ms-2 min-h-12 rounded-2xl border border-line bg-transparent px-3" />
              </label>
              <button type="submit" className="min-h-12 rounded-2xl border border-line px-4">مقارنة المراجعتين</button>
            </form>
          ) : null}
          <ul className="space-y-3">
            {diff.changes.map((change) => (
              <li key={`${change.code}-${change.field}`} className="rounded-[1.5rem] border border-line bg-raised p-4">
                <p>{change.label}</p>
                <div className="mt-3 grid gap-3 md:grid-cols-2">
                  <p className="rounded-2xl border border-line p-3">{change.textDiff.filter((part) => part.op !== "INSERT").map((part) => part.text).join("")}</p>
                  <p className="rounded-2xl border border-line p-3">{change.textDiff.filter((part) => part.op !== "DELETE").map((part) => part.text).join("")}</p>
                </div>
              </li>
            ))}
          </ul>
        </section>
      ) : null}
    </main>
  );
}
