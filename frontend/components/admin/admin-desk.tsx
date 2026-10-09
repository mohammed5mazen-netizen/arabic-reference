"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useSyncExternalStore, type FormEvent, type ReactNode } from "react";
import { ThemeToggle } from "@/components/theme-toggle";
import {
  adminFetch,
  clearAdminTokens,
  logoutAdmin,
  readAdminAccessToken,
  storeAdminTokens,
  type AdminSession,
} from "@/lib/admin-api";
import { visibleAdminNav } from "@/lib/admin-nav";

let session: AdminSession | null = null;
let loaded = false;
const listeners = new Set<() => void>();

function emit() {
  listeners.forEach((listener) => listener());
  window.dispatchEvent(new Event("ar-admin-session"));
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  const onStorage = () => listener();
  window.addEventListener("ar-admin-session", onStorage);
  return () => {
    listeners.delete(listener);
    window.removeEventListener("ar-admin-session", onStorage);
  };
}

function snapshot(): AdminSession | null {
  return session;
}

function publishSession(next: AdminSession | null) {
  session = next;
  emit();
}

function requestSession(onMissing: () => void) {
  if (loaded) {
    return;
  }
  loaded = true;
  void adminFetch<AdminSession>("/api/v1/admin/auth/session")
    .then(publishSession)
    .catch(() => {
      clearAdminTokens(sessionStorage);
      loaded = false;
      publishSession(null);
      onMissing();
    });
}

export function AdminDesk({ children }: { children: ReactNode }) {
  const router = useRouter();
  const current = useSyncExternalStore(subscribe, snapshot, () => null);

  useEffect(() => {
    if (!readAdminAccessToken(sessionStorage)) {
      router.replace("/admin/login");
      return;
    }
    requestSession(() => router.replace("/admin/login"));
  }, [router]);

  if (!current) {
    return <p className="px-5 py-16 text-muted">جارٍ فتح مكتب التحرير...</p>;
  }

  const items = visibleAdminNav(current.permissions);

  return (
    <div className="relative z-10 mx-auto grid min-h-screen w-full max-w-6xl gap-6 px-4 py-6 lg:grid-cols-[16rem_1fr]">
      <aside className="rounded-[2rem] border border-line bg-raised p-5">
        <p className="font-display text-2xl">مكتب التحرير</p>
        <p className="mt-2 text-sm text-muted">{current.displayName}</p>
        <nav aria-label="إدارة التحرير" className="mt-6 space-y-1">
          {items.map((item) => (
            <Link key={item.href} href={item.href} className="block rounded-2xl px-3 py-2 hover:bg-library-soft">
              {item.label}
            </Link>
          ))}
        </nav>
        <div className="mt-6 flex gap-2">
          <ThemeToggle />
          <button type="button" onClick={() => void logoutAdmin().then(() => router.replace("/admin/login"))} className="rounded-full border border-line px-4 py-2">
            خروج
          </button>
        </div>
      </aside>
      <div className="min-w-0">
        {current.mustChangePassword ? <PasswordChange onChanged={publishSession} /> : children}
      </div>
    </div>
  );
}

function PasswordChange({ onChanged }: { onChanged: (session: AdminSession) => void }) {
  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const tokens = await adminFetch<{ accessToken: string; refreshToken: string }>("/api/v1/admin/auth/change-password", {
      method: "POST",
      body: JSON.stringify({
        currentPassword: String(form.get("currentPassword") ?? ""),
        newPassword: String(form.get("newPassword") ?? ""),
      }),
    });
    storeAdminTokens(sessionStorage, { ...tokens, mustChangePassword: false });
    const next = await adminFetch<AdminSession>("/api/v1/admin/auth/session");
    onChanged(next);
  }

  return (
    <main id="content" className="rounded-[2rem] border border-line bg-raised p-8">
      <h1 className="font-display text-4xl">تغيير كلمة المرور المؤقتة</h1>
      <p className="mt-3 text-muted">لا يمكن استخدام أدوات الإدارة قبل اختيار كلمة مرور جديدة.</p>
      <form onSubmit={onSubmit} className="mt-6 space-y-4">
        <label className="block" htmlFor="current-password">
          كلمة المرور الحالية
          <input id="current-password" name="currentPassword" type="password" required className="mt-2 w-full rounded-2xl border border-line bg-paper px-4 py-3" />
        </label>
        <label className="block" htmlFor="new-password">
          كلمة المرور الجديدة
          <input id="new-password" name="newPassword" type="password" required minLength={12} className="mt-2 w-full rounded-2xl border border-line bg-paper px-4 py-3" />
        </label>
        <button type="submit" className="rounded-full bg-library px-5 py-3 text-[var(--paper)]">
          حفظ كلمة المرور
        </button>
      </form>
    </main>
  );
}
