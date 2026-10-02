"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";

type EventRow = { id: string; eventType: string; actorId: string | null; occurredAt: string; traceId: string };
type Page = { items: EventRow[] };

export default function AuditPage() {
  const [page, setPage] = useState<Page | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<Page>("/api/v1/admin/audit")
      .then((next) => {
        if (!cancelled) setPage(next);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر قراءة السجل.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <main id="content">
      <h1 className="font-display text-5xl">سجل التدقيق</h1>
      {error ? <p role="alert" className="mt-4">{error}</p> : null}
      <ul className="mt-6 space-y-3">
        {page?.items.map((event) => (
          <li key={event.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <p className="font-medium">{event.eventType}</p>
            <p className="text-sm text-muted">
              <time dateTime={event.occurredAt}>{event.occurredAt}</time>
              {" · "}
              {event.traceId}
            </p>
          </li>
        ))}
      </ul>
    </main>
  );
}
