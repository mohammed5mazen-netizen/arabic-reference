"use client";

import { useEffect, useId, useState, type FormEvent, type KeyboardEvent } from "react";
import { useRouter } from "next/navigation";
import { searchPath } from "@/lib/dictionary";
import { searchDebounceMs, suggestionIndex, suggestionUrl, type SearchSuggestion } from "@/lib/search";
import { searchPlaceholder } from "@/lib/site";

export function SearchPanel({ initialQuery = "", variant = "page" }: { initialQuery?: string; variant?: "page" | "header" }) {
  const inputId = useId();
  const listId = useId();
  const router = useRouter();
  const [query, setQuery] = useState(initialQuery);
  const [suggestions, setSuggestions] = useState<SearchSuggestion[]>([]);
  const [active, setActive] = useState(-1);

  useEffect(() => {
    const controller = new AbortController();
    const handle = setTimeout(() => {
      const url = suggestionUrl(query);
      if (!url) {
        Promise.resolve().then(() => {
          if (!controller.signal.aborted) {
            setSuggestions([]);
            setActive(-1);
          }
        });
        return;
      }
      fetch(url, { signal: controller.signal })
        .then((response) => (response.ok ? response.json() : { data: [] }))
        .then((body: { data?: SearchSuggestion[] }) => {
          if (!controller.signal.aborted) {
            setSuggestions((body.data ?? []).slice(0, 8));
            setActive(-1);
          }
        })
        .catch(() => {
          if (!controller.signal.aborted) setSuggestions([]);
        });
    }, searchDebounceMs);
    return () => {
      controller.abort();
      clearTimeout(handle);
    };
  }, [query]);

  function go(value: string) {
    const next = value.trim();
    if (!next) return;
    router.push(searchPath(next));
  }

  function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (active >= 0 && suggestions[active]) {
      router.push(suggestions[active].url);
      return;
    }
    go(query);
  }

  function onKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    const next = suggestionIndex(active, event.key, suggestions.length);
    if (next === "close") {
      setSuggestions([]);
      setActive(-1);
      return;
    }
    if (next === "choose") return;
    if (typeof next === "number" && next !== active) {
      event.preventDefault();
      setActive(next);
    }
  }

  const open = suggestions.length > 0;

  const header = variant === "header";

  return (
    <form role="search" className={header ? "" : "mt-8"} onSubmit={onSubmit}>
      <label htmlFor={inputId} className={header ? "sr-only" : "mb-3 block text-sm font-medium text-muted"}>
        البحث في المرجع
      </label>
      <div className="flex flex-col gap-3 rounded-[1.75rem] border border-line bg-raised p-3 shadow-[var(--shadow)] sm:flex-row sm:items-center">
        <input
          id={inputId}
          name="q"
          type="search"
          role="combobox"
          aria-expanded={open}
          aria-controls={listId}
          aria-autocomplete="list"
          aria-activedescendant={active >= 0 ? `${listId}-${active}` : undefined}
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={onKeyDown}
          placeholder={searchPlaceholder}
          autoComplete="off"
          className="min-h-14 w-full bg-transparent px-4 text-lg text-ink outline-none placeholder:text-muted"
        />
        <button type="submit" className="min-h-14 rounded-2xl bg-library px-8 text-base font-semibold text-[var(--paper)]">
          بحث
        </button>
      </div>
      <ul id={listId} role="listbox" aria-label="اقتراحات البحث" className="mt-2 overflow-hidden rounded-2xl border border-line bg-raised empty:hidden">
        {suggestions.map((item, index) => (
          <li key={`${item.url}-${index}`} id={`${listId}-${index}`} role="option" aria-selected={index === active}>
            <button
              type="button"
              className="flex min-h-12 w-full items-center justify-between px-4 text-start"
              onMouseDown={(event) => {
                event.preventDefault();
                router.push(item.url);
              }}
            >
              <span>{item.title}</span>
              <span className="text-sm text-muted">{item.kind === "root" ? "جذر" : item.kind === "grammar_concept" ? "مصطلح" : item.kind === "grammar_topic" ? "موضوع" : "كلمة"}</span>
            </button>
          </li>
        ))}
      </ul>
      {header ? null : <p className="mt-4 min-h-6 text-sm text-muted">مربع واحد للمعجم والجذور والنحو والمحتوى المنشور. النتائج من المحتوى المنشور.</p>}
    </form>
  );
}
