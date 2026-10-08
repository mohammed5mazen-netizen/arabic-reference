"use client";

import { useSyncExternalStore } from "react";

const storageKey = "arabic-reference-theme";

type Theme = "light" | "dark";

function subscribe(onStoreChange: () => void) {
  const observer = new MutationObserver(onStoreChange);
  observer.observe(document.documentElement, { attributes: true, attributeFilter: ["class"] });
  return () => observer.disconnect();
}

function snapshot(): Theme {
  return document.documentElement.classList.contains("dark") ? "dark" : "light";
}

function serverSnapshot(): Theme {
  return "light";
}

export function ThemeToggle() {
  const theme = useSyncExternalStore(subscribe, snapshot, serverSnapshot);
  const isDark = theme === "dark";

  function toggle() {
    const next: Theme = isDark ? "light" : "dark";
    document.documentElement.classList.toggle("dark", next === "dark");
    document.documentElement.style.colorScheme = next;
    localStorage.setItem(storageKey, next);
  }

  return (
    <button
      type="button"
      onClick={toggle}
      aria-pressed={isDark}
      aria-label={isDark ? "التبديل إلى الوضع الفاتح" : "التبديل إلى الوضع الداكن"}
      className="min-h-11 rounded-full border border-line bg-raised px-4 py-2 text-sm text-ink shadow-sm"
    >
      {isDark ? "الوضع الفاتح" : "الوضع الداكن"}
    </button>
  );
}
