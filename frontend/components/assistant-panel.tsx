"use client";

import { useEffect, useId, useRef, useState, type FormEvent } from "react";
import Link from "next/link";
import { assistantApiBase, assistantDisabled, assistantPlaceholder, type AssistantAnswer, type AssistantStatus } from "@/lib/assistant";

export function AssistantPanel({ initialQuestion = "" }: { initialQuestion?: string }) {
  const inputId = useId();
  const errorId = useId();
  const answerRef = useRef<HTMLElement>(null);
  const [status, setStatus] = useState<AssistantStatus | null>(null);
  const [question, setQuestion] = useState(initialQuestion);
  const [answer, setAnswer] = useState<AssistantAnswer | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let cancelled = false;
    void fetch(`${assistantApiBase()}/api/v1/public/ai/status`, { cache: "no-store" })
      .then((response) => response.json())
      .then((body: { data?: AssistantStatus }) => {
        if (!cancelled) setStatus(body.data ?? null);
      })
      .catch(() => {
        if (!cancelled) setStatus({ available: false, message: assistantDisabled, suggestions: [] });
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    answerRef.current?.focus();
  }, [answer]);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const response = await fetch(`${assistantApiBase()}/api/v1/public/ai/ask`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ question }),
      });
      const body = await response.json();
      if (!response.ok) {
        setAnswer(null);
        setError(typeof body.message === "string" ? body.message : "تعذر إكمال الإجابة الآن.");
        return;
      }
      setAnswer(body.data as AssistantAnswer);
    } catch {
      setError("تعذر إكمال الإجابة الآن. جرّب مرة أخرى بعد قليل أو استخدم البحث.");
    } finally {
      setLoading(false);
    }
  }

  if (status && !status.available) {
    return (
      <section className="mt-8 rounded-3xl border border-line bg-raised p-6" aria-labelledby="assistant-unavailable">
        <h2 id="assistant-unavailable" className="font-display text-3xl">المساعد غير متاح حاليًا</h2>
        <p className="mt-3 leading-8">{status.message}</p>
        <p className="mt-4 flex flex-wrap gap-4">
          <Link href="/search">البحث</Link>
          <Link href="/tools">الأدوات</Link>
        </p>
      </section>
    );
  }

  return (
    <div className="mt-8">
      <form onSubmit={submit} className="space-y-4">
        <label htmlFor={inputId} className="block text-sm">سؤالك</label>
        <textarea
          id={inputId}
          value={question}
          onChange={(event) => setQuestion(event.target.value)}
          placeholder={assistantPlaceholder}
          rows={4}
          required
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? errorId : undefined}
          className="min-h-32 w-full rounded-3xl border border-line bg-raised px-4 py-3 text-lg leading-8"
        />
        {status?.suggestions?.length ? (
          <ul className="flex flex-wrap gap-2">
            {status.suggestions.map((suggestion) => (
              <li key={suggestion}>
                <button type="button" className="rounded-full border border-line px-3 py-1 text-sm" onClick={() => setQuestion(suggestion)}>
                  {suggestion}
                </button>
              </li>
            ))}
          </ul>
        ) : null}
        <button type="submit" className="min-h-12 rounded-2xl bg-library px-6 text-white" disabled={loading}>
          {loading ? "يجري إعداد الإجابة" : "اسأل"}
        </button>
        <p role="status" className="sr-only">{loading ? "يجري إعداد الإجابة" : ""}</p>
        {error ? <p id={errorId} role="alert">{error}</p> : null}
      </form>
      {answer ? (
        <article ref={answerRef} tabIndex={-1} className="mt-8 space-y-6 outline-none" aria-labelledby="assistant-answer">
          <div>
            <p className="text-sm text-library">{answer.groundingLabel}</p>
            {answer.uncertain ? <p className="text-sm">يتضمن تحليلًا احتماليًا</p> : null}
            <h2 id="assistant-answer" className="mt-2 font-display text-4xl">الإجابة</h2>
            <p className="mt-4 whitespace-pre-wrap leading-8">{answer.answer}</p>
          </div>
          {answer.evidence.length ? (
            <section aria-labelledby="assistant-evidence">
              <h3 id="assistant-evidence" className="font-display text-3xl">اعتمدت الإجابة على</h3>
              <ul className="mt-4 grid grid-cols-1 gap-4">
                {answer.evidence.map((item) => (
                  <li key={item.evidenceId} className="rounded-3xl border border-line bg-raised p-5">
                    <p className="text-sm text-muted">{item.typeLabel}</p>
                    <h4 className="mt-1 font-display text-2xl">{item.title}</h4>
                    <p className="mt-2 leading-7">{item.excerpt}</p>
                    {item.sourceLabel ? <p className="mt-2 text-sm">المصدر: {item.sourceLabel}</p> : null}
                    <p className="mt-2 text-sm">{item.provenanceLabel}</p>
                    <Link href={item.href}>اقرأ المزيد: {item.title}</Link>
                  </li>
                ))}
              </ul>
            </section>
          ) : null}
          {answer.limitations.length ? (
            <section aria-labelledby="assistant-limits">
              <h3 id="assistant-limits" className="font-display text-2xl">قيود الإجابة</h3>
              <ul className="mt-2 list-disc pe-5 leading-8">
                {answer.limitations.map((item) => <li key={item}>{item}</li>)}
              </ul>
            </section>
          ) : null}
          {answer.nearby.length ? (
            <section aria-labelledby="assistant-nearby">
              <h3 id="assistant-nearby" className="font-display text-2xl">نتائج قريبة</h3>
              <ul className="mt-2 space-y-2">
                {answer.nearby.map((item) => (
                  <li key={item.href}><Link href={item.href}>{item.title}</Link> <span className="text-sm text-muted">{item.note}</span></li>
                ))}
              </ul>
            </section>
          ) : null}
          <ul className="flex flex-wrap gap-4">
            {answer.tools.map((tool) => <li key={tool.href}><Link href={tool.href}>{tool.label}</Link></li>)}
          </ul>
        </article>
      ) : null}
    </div>
  );
}
