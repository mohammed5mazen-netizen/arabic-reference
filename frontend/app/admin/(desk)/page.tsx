"use client";

import { useSyncExternalStore } from "react";
import Link from "next/link";
import { adminFetch } from "@/lib/admin-api";
import type { EditorialDashboard } from "@/lib/editorial";

type Dashboard = {
  users: { total: number; active: number; locked: number } | null;
  recentAudit: { id: string; eventType: string; occurredAt: string }[] | null;
  editorial: EditorialDashboard | null;
};

let dashboard: Dashboard | undefined;
const listeners = new Set<() => void>();

function load() {
  if (dashboard === undefined) {
    dashboard = { users: null, recentAudit: null, editorial: null };
    void Promise.all([
      adminFetch<Omit<Dashboard, "editorial">>("/api/v1/admin/dashboard").catch(() => null),
      adminFetch<EditorialDashboard>("/api/v1/admin/editorial/dashboard").catch(() => null),
    ]).then(([home, editorial]) => {
      dashboard = {
        users: home?.users ?? null,
        recentAudit: home?.recentAudit ?? null,
        editorial,
      };
      listeners.forEach((listener) => listener());
    });
  }
  return dashboard;
}

export default function AdminHomePage() {
  const data = useSyncExternalStore(
    (listener) => {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
    load,
    () => ({ users: null, recentAudit: null, editorial: null }),
  );

  return (
    <main id="content" className="space-y-6">
      <header>
        <h1 className="font-display text-5xl">مكتب التحرير</h1>
        <p className="mt-3 text-muted">ملخص التشغيل يفتح غرفة العمليات عندما تملك صلاحيتها.</p>
      </header>
      {data.editorial ? (
        <section className="grid gap-4 sm:grid-cols-3">
          <Stat label="بانتظار المراجعة" value={String(data.editorial.inReview)} />
          <Stat label="جاهز للنشر" value={String(data.editorial.readyToPublish)} />
          <Stat label="ملاحظات الجودة" value={String(data.editorial.qualityIssues)} />
          <Link className="underline" href="/admin/editorial">غرفة العمليات</Link>
        </section>
      ) : null}
      <section className="grid gap-4 sm:grid-cols-3">
        <Stat label="المستخدمون الإداريون" value={data.users ? String(data.users.total) : "غير متاح"} />
        <Stat label="النشطون" value={data.users ? String(data.users.active) : "غير متاح"} />
        <Stat label="المقفولون" value={data.users ? String(data.users.locked) : "غير متاح"} />
      </section>
      <section className="rounded-[2rem] border border-line bg-raised p-6">
        <h2 className="font-display text-3xl">آخر أحداث التدقيق</h2>
        {data.recentAudit == null ? <p className="mt-4 text-muted">سجل التدقيق غير متاح لهذه الصلاحيات.</p> : null}
        {data.recentAudit?.length === 0 ? <p className="mt-4 text-muted">لا توجد أحداث بعد.</p> : null}
        <ul className="mt-4 space-y-3">
          {data.recentAudit?.map((event) => (
            <li key={event.id} className="flex items-center justify-between gap-4 border-b border-line py-2">
              <span>{event.eventType}</span>
              <time dateTime={event.occurredAt}>{event.occurredAt}</time>
            </li>
          ))}
        </ul>
      </section>
    </main>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <article className="rounded-[2rem] border border-line bg-raised p-5">
      <p className="text-sm text-muted">{label}</p>
      <p className="mt-3 font-display text-4xl">{value}</p>
    </article>
  );
}
