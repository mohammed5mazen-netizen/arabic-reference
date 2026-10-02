"use client";

import { useEffect, useState, type FormEvent } from "react";
import { adminFetch } from "@/lib/admin-api";
import { can } from "@/lib/admin-nav";

type UserRow = {
  id: string;
  username: string;
  email: string;
  displayName: string;
  status: string;
  version: number;
  roles: { code: string; name: string }[];
};

type Page = { items: UserRow[]; total: number };
type Session = { permissions: string[] };

export default function UsersPage() {
  const [page, setPage] = useState<Page | null>(null);
  const [permissions, setPermissions] = useState<string[]>([]);
  const [error, setError] = useState("");
  const [temporaryPassword, setTemporaryPassword] = useState("");

  useEffect(() => {
    let cancelled = false;
    void Promise.all([
      adminFetch<Page>("/api/v1/admin/users"),
      adminFetch<Session>("/api/v1/admin/auth/session"),
    ])
      .then(([users, session]) => {
        if (cancelled) return;
        setPage(users);
        setPermissions(session.permissions);
      })
      .catch((caught: unknown) => {
        if (!cancelled) setError(caught instanceof Error ? caught.message : "تعذر تحميل المستخدمين.");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  async function createUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const created = await adminFetch<{ user: UserRow; temporaryPassword: string }>("/api/v1/admin/users", {
      method: "POST",
      body: JSON.stringify({
        username: String(form.get("username") ?? ""),
        email: String(form.get("email") ?? ""),
        displayName: String(form.get("displayName") ?? ""),
        roleIds: [],
      }),
    });
    setTemporaryPassword(created.temporaryPassword);
    setPage(await adminFetch<Page>("/api/v1/admin/users"));
    event.currentTarget.reset();
  }

  async function act(user: UserRow, action: "deactivate" | "activate" | "unlock") {
    await adminFetch(`/api/v1/admin/users/${user.id}/${action}`, {
      method: "POST",
      body: JSON.stringify({ version: user.version }),
    });
    setPage(await adminFetch<Page>("/api/v1/admin/users"));
  }

  return (
    <main id="content" className="space-y-6">
      <h1 className="font-display text-5xl">المستخدمون</h1>
      {error ? <p role="alert">{error}</p> : null}
      <ul className="space-y-3">
        {page?.items.map((user) => (
          <li key={user.id} className="rounded-[1.5rem] border border-line bg-raised p-4">
            <p className="font-medium">{user.displayName}</p>
            <p className="text-sm text-muted">{user.username} · {user.email} · {user.status}</p>
            <p className="text-sm text-muted">{user.roles.map((role) => role.name).join("، ") || "بلا دور"}</p>
            <div className="mt-3 flex flex-wrap gap-2">
              {can(permissions, "admin.user.deactivate") && user.status === "ACTIVE" ? (
                <button type="button" onClick={() => void act(user, "deactivate")} className="rounded-full border border-line px-3 py-1">تعطيل</button>
              ) : null}
              {can(permissions, "admin.user.activate") && user.status === "DISABLED" ? (
                <button type="button" onClick={() => void act(user, "activate")} className="rounded-full border border-line px-3 py-1">تفعيل</button>
              ) : null}
              {can(permissions, "admin.user.unlock") && user.status === "LOCKED" ? (
                <button type="button" onClick={() => void act(user, "unlock")} className="rounded-full border border-line px-3 py-1">فك القفل</button>
              ) : null}
            </div>
          </li>
        ))}
      </ul>
      {can(permissions, "admin.user.create") ? (
        <form onSubmit={createUser} className="space-y-3 rounded-[1.5rem] border border-line bg-raised p-5">
          <h2 className="font-display text-3xl">حساب تحريري جديد</h2>
          <input name="username" required placeholder="اسم المستخدم" aria-label="اسم المستخدم" className="w-full rounded-2xl border border-line bg-paper px-4 py-3" />
          <input name="email" type="email" required placeholder="البريد" aria-label="البريد" className="w-full rounded-2xl border border-line bg-paper px-4 py-3" />
          <input name="displayName" required placeholder="الاسم الظاهر" aria-label="الاسم الظاهر" className="w-full rounded-2xl border border-line bg-paper px-4 py-3" />
          <button type="submit" className="rounded-full bg-library px-5 py-3 text-[var(--paper)]">إنشاء</button>
          {temporaryPassword ? <p>كلمة المرور المؤقتة، تُعرض مرة واحدة: {temporaryPassword}</p> : null}
        </form>
      ) : null}
    </main>
  );
}
