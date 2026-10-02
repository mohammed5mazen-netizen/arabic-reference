"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { adminApiBase, requestLogin, storeAdminTokens } from "@/lib/admin-api";

export function LoginForm() {
  const router = useRouter();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setError("");
    try {
      const tokens = await requestLogin(adminApiBase, username, password);
      storeAdminTokens(sessionStorage, tokens);
      window.dispatchEvent(new Event("ar-admin-session"));
      router.replace("/admin");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "تعذر الدخول.");
    } finally {
      setPending(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="space-y-5" noValidate>
      <div>
        <label htmlFor="admin-username" className="block text-sm font-medium">
          اسم المستخدم
        </label>
        <input
          id="admin-username"
          name="username"
          autoComplete="username"
          required
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          className="mt-2 w-full rounded-2xl border border-line bg-raised px-4 py-3 text-ink"
        />
      </div>
      <div>
        <label htmlFor="admin-password" className="block text-sm font-medium">
          كلمة المرور
        </label>
        <input
          id="admin-password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          className="mt-2 w-full rounded-2xl border border-line bg-raised px-4 py-3 text-ink"
        />
      </div>
      {error ? (
        <p role="alert" className="text-seal">
          {error}
        </p>
      ) : null}
      <button
        type="submit"
        disabled={pending}
        className="inline-flex min-h-12 w-full items-center justify-center rounded-full bg-library px-6 font-medium text-[var(--paper)] disabled:opacity-60"
      >
        {pending ? "جارٍ التحقق..." : "دخول الإدارة"}
      </button>
      <p className="text-sm leading-7 text-muted">
        <Link href="/">العودة إلى المرجع</Link>
        . القراءة العامة لا تحتاج إلى هذا الدخول.
      </p>
    </form>
  );
}
