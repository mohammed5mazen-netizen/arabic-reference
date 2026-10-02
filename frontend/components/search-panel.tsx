"use client";

import { useId, useState } from "react";
import { searchPlaceholder } from "@/lib/site";

export function SearchPanel() {
  const inputId = useId();
  const noticeId = useId();
  const [notice, setNotice] = useState(false);

  return (
    <form
      role="search"
      className="mt-8"
      onSubmit={(event) => {
        event.preventDefault();
        setNotice(true);
      }}
    >
      <label htmlFor={inputId} className="mb-3 block text-sm font-medium text-muted">
        البحث في المرجع
      </label>
      <div className="flex flex-col gap-3 rounded-[1.75rem] border border-line bg-raised p-3 shadow-[var(--shadow)] sm:flex-row sm:items-center">
        <input
          id={inputId}
          name="q"
          type="search"
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
      <p id={noticeId} role="status" aria-live="polite" className="mt-4 min-h-6 text-sm text-muted">
        {notice
          ? "البحث العربي المتقدم لم يُفعَّل بعد. هذه المرحلة تؤسس للمنصة، ولا تعرض نتائج وهمية."
          : "اكتب سؤالك اللغوي هنا. النتائج ستصل مع محرك البحث، دون الحاجة إلى حساب."}
      </p>
    </form>
  );
}
