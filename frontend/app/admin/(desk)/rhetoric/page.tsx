"use client";

import { useEffect, useState } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";
import { rhetoricAdminSections } from "@/lib/knowledge";
import { KnowledgeReview } from "@/components/knowledge-review";

type Session = { permissions: string[] };
type Created = { id: string; version: number };

export default function AdminRhetoricPage() {
  const [permissions, setPermissions] = useState<string[]>([]);
  const [title, setTitle] = useState("");
  const [topicId, setTopicId] = useState("");
  const [deviceId, setDeviceId] = useState("");
  const [targetId, setTargetId] = useState("");
  const [version, setVersion] = useState(0);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void adminFetch<Session>("/api/v1/admin/auth/session").then((session) => {
      if (!cancelled) setPermissions(session.permissions);
    }).catch((caught: unknown) => {
      if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر فتح البلاغة.");
    });
    return () => {
      cancelled = true;
    };
  }, []);

  function fail(caught: unknown) {
    setError(caught instanceof Error ? caught.message : "تعذر الحفظ.");
  }

  return (
    <main id="content" className="space-y-8">
      <h1 className="font-display text-5xl">البلاغة</h1>
      <nav aria-label="أقسام البلاغة" className="flex flex-wrap gap-3">{rhetoricAdminSections.map((section) => <span key={section}>{section}</span>)}</nav>
      {error ? <p role="alert">{error}</p> : null}
      {can(permissions, "rhetoric.topic.manage") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الموضوعات</h2>
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="العنوان" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void adminFetch<Created>("/api/v1/admin/rhetoric/topics", { method: "POST", body: JSON.stringify({ category: "BAYAN", title, summary: title, displayOrder: 1 }) }).then((created) => setTopicId(created.id)).catch(fail)}>إنشاء موضوع</button>
        </section>
      ) : null}
      {can(permissions, "rhetoric.device.create") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الفنون</h2>
          <input value={topicId} onChange={(event) => setTopicId(event.target.value)} placeholder="معرّف الموضوع" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <button type="button" className="min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void adminFetch<Created>("/api/v1/admin/rhetoric/devices", { method: "POST", body: JSON.stringify({ topicId, name: title, shortDefinition: title, detailedExplanation: title }) }).then((created) => { setDeviceId(created.id); setVersion(created.version); }).catch(fail)}>إنشاء فن</button>
        </section>
      ) : null}
      {can(permissions, "rhetoric.device.edit") ? (
        <section className="space-y-3">
          <h2 className="font-display text-3xl">الأمثلة والصلات</h2>
          <input value={deviceId} onChange={(event) => setDeviceId(event.target.value)} placeholder="معرّف الفن" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <input value={targetId} onChange={(event) => setTargetId(event.target.value)} placeholder="فن مقابل" className="min-h-12 w-full rounded-2xl border border-line bg-transparent px-4" />
          <div className="flex flex-wrap gap-3">
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<Created>(`/api/v1/admin/rhetoric/devices/${deviceId}/examples`, { method: "POST", body: JSON.stringify({ version, kind: "CONSTRUCTED", text: title, explanation: title, alternativeInterpretation: "فهم آخر" }) }).then((created) => setVersion(created.version)).catch(fail)}>إضافة مثال</button>
            <button type="button" className="min-h-12 rounded-2xl border border-line px-4" onClick={() => void adminFetch<Created>(`/api/v1/admin/rhetoric/devices/${deviceId}/relations`, { method: "POST", body: JSON.stringify({ version, targetDeviceId: targetId, kind: "CONTRASTS_WITH" }) }).then((created) => setVersion(created.version)).catch(fail)}>ربط فن</button>
          </div>
        </section>
      ) : null}
      <KnowledgeReview endpoint="/api/v1/admin/rhetoric/review" paths={{ TOPIC: "/api/v1/admin/rhetoric/topics", DEVICE: "/api/v1/admin/rhetoric/devices" }} editPermission="rhetoric.device.edit" reviewPermission="rhetoric.device.review" publishPermission="rhetoric.device.publish" />
    </main>
  );
}
