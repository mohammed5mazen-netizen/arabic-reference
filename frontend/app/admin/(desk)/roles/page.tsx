"use client";

import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";

type Role = { id: string; code: string; name: string; description: string; systemRole: boolean; permissions: string[] };
type Session = { permissions: string[] };

export default function RolesPage() {
  const [roles, setRoles] = useState<Role[] | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Role[]>("/api/v1/admin/roles"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([nextRoles, session]) => {
        if (cancelled) return;
        setRoles(nextRoles);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل الأدوار.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createRole(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await adminFetch("/api/v1/admin/roles", {
      method: "POST",
      body: JSON.stringify({
        code: String(form.get("code") ?? ""),
        name: String(form.get("name") ?? ""),
        description: String(form.get("description") ?? ""),
        permissionCodes: String(form.get("permissionCodes") ?? "")
          .split(",")
          .map((code) => code.trim())
          .filter(Boolean),
      }),
    });
    setRoles(await adminFetch<Role[]>("/api/v1/admin/roles"));
    event.currentTarget.reset();
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">الأدوار والصلاحيات</h1>
      {error ? <p role="alert">{error}</p> : null}
      <ul className="space-y-3">
        {roles?.map((role) => (
          <li key={role.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <p className="font-medium">{role.name}</p>
            <p className="text-sm text-muted">{role.code}{role.systemRole ? " · دور نظام" : ""}</p>
            <p className="mt-2 text-sm leading-7">{role.permissions.join("، ")}</p>
          </li>
        ))}
      </ul>
      {can(permissions, "admin.role.create") ? (
        <form onSubmit={createRole} className="space-y-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <h2 className="font-display text-3xl">دور مخصص</h2>
          <input name="code" required aria-label="رمز الدور" placeholder="رمز الدور" className="w-full rounded-2xl border border-line bg-paper px-4 py-3" />
          <input name="name" required aria-label="اسم الدور" placeholder="اسم الدور" className="w-full rounded-2xl border border-line bg-paper px-4 py-3" />
          <input name="description" required aria-label="وصف الدور" placeholder="وصف الدور" className="w-full rounded-2xl border border-line bg-paper px-4 py-3" />
          <input name="permissionCodes" aria-label="الصلاحيات" placeholder="صلاحيات مفصولة بفاصلة، مما تملكه أنت" className="w-full rounded-2xl border border-line bg-paper px-4 py-3" />
          <button type="submit" className="rounded-full bg-library px-5 py-3 text-[var(--paper)]">إنشاء الدور</button>
        </form>
      ) : null}
    </main>
  );
}
