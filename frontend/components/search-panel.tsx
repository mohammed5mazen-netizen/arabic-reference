"use client";

import { useId, useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";
import { searchPath } from "@/lib/dictionary";
import { searchPlaceholder } from "@/lib/site";

export function SearchPanel() {
  const inputId = useId();
  const router = useRouter();
  const [query, setQuery] = useState("");

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const value = query.trim();
    if (!value) return;
    router.push(searchPath(value));
  }

  return (
    <form role="search" className="mt-8" onSubmit={onSubmit}>
      <label htmlFor={inputId} className="mb-3 block text-sm font-medium text-muted">
        البحث في المرجع
      </label>
      <div className="flex flex-col gap-3 rounded-[1.75rem] border border-line bg-raised p-3 shadow-[var(--shadow)] sm:flex-row sm:items-center">
        <input
          id={inputId}
          name="q"
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={searchPlaceholder}
          autoComplete="off"
          className="min-h-14 w-full bg-transparent px-4 text-lg text-ink outline-none placeholder:text-muted"
        />
        <button
          type="submit"
          className="min-h-14 rounded-2xl bg-library px-8 text-base font-semibold text-[var(--paper)]"
        >
          بحث
        </button>
      </div>
      <p className="mt-4 min-h-6 text-sm text-muted">اكتب كلمة عربية. البحث يطابق الشكل المطبّع دون أن يبدّل النص المعروض.</p>
    </form>
  );
}
