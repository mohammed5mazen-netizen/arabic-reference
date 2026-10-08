"use client";

import { useId, useState, type FormEvent } from "react";
import Link from "next/link";
import { apiBase } from "@/lib/dictionary";
import { passingLabel, type QuizResult, type QuizStart } from "@/lib/learning";
import { saveQuizScore } from "@/lib/learning-progress";

export function LessonQuiz({ quiz }: { quiz: { id: string; title: string; passingScore: number; questionCount: number } }) {
  const errorId = useId();
  const [attempt, setAttempt] = useState<QuizStart | null>(null);
  const [selected, setSelected] = useState<Record<string, string[]>>({});
  const [result, setResult] = useState<QuizResult | null>(null);
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  async function start() {
    setError("");
    setResult(null);
    setSelected({});
    setPending(true);
    try {
      const response = await fetch(`${apiBase()}/api/v1/public/learning/quizzes/${quiz.id}/attempts`, { method: "POST" });
      const body = await response.json();
      if (!response.ok) {
        setError(typeof body.message === "string" ? body.message : "تعذر بدء الاختبار.");
        return;
      }
      setAttempt(body.data as QuizStart);
    } catch {
      setError("تعذر بدء الاختبار.");
    } finally {
      setPending(false);
    }
  }

  function choose(questionId: string, optionId: string, multiple: boolean) {
    setSelected((current) => {
      const existing = current[questionId] ?? [];
      if (!multiple) return { ...current, [questionId]: [optionId] };
      const next = existing.includes(optionId) ? existing.filter((id) => id !== optionId) : [...existing, optionId];
      return { ...current, [questionId]: next };
    });
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!attempt) return;
    if (attempt.questions.some((question) => (selected[question.id] ?? []).length === 0)) {
      setError("أجب عن كل الأسئلة قبل الإرسال.");
      return;
    }
    setPending(true);
    setError("");
    const idempotencyKey = crypto.randomUUID();
    try {
      const response = await fetch(`${apiBase()}/api/v1/public/learning/attempts/${attempt.token}/submit`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          idempotencyKey,
          answers: attempt.questions.map((question) => ({ questionId: question.id, optionIds: selected[question.id] ?? [] })),
        }),
      });
      const body = await response.json();
      if (!response.ok) {
        setError(typeof body.message === "string" ? body.message : "تعذر تصحيح الاختبار.");
        return;
      }
      const scored = body.data as QuizResult;
      setResult(scored);
      saveQuizScore(localStorage, quiz.id, scored.score, scored.passed);
    } catch {
      setError("تعذر تصحيح الاختبار.");
    } finally {
      setPending(false);
    }
  }

  return (
    <section className="mt-10 rounded-3xl border border-line bg-raised p-5" aria-labelledby="quiz-title">
      <h2 id="quiz-title" className="font-display text-3xl">{quiz.title}</h2>
      <p className="mt-2 text-sm text-muted">{quiz.questionCount} أسئلة. درجة النجاح {quiz.passingScore}.</p>
      {error ? <p id={errorId} role="alert" className="mt-3">{error}</p> : null}
      {!attempt || result ? (
        <button type="button" className="mt-4 min-h-12 rounded-2xl bg-library px-4 text-white" onClick={() => void start()} disabled={pending}>
          {result ? "حاول مرة أخرى" : "ابدأ الاختبار"}
        </button>
      ) : null}
      {attempt && !result ? (
        <form onSubmit={(event) => void submit(event)} className="mt-6 space-y-6" aria-describedby={error ? errorId : undefined}>
          {attempt.questions.map((question, index) => {
            const multiple = question.type === "MULTIPLE_SELECT";
            return (
              <fieldset key={question.id} className="space-y-3">
                <legend className="font-display text-2xl">{index + 1}. {question.prompt}</legend>
                {question.options.map((option) => (
                  <label key={option.id} className="flex min-h-14 cursor-pointer items-center gap-3 rounded-2xl border border-line px-4">
                    <input
                      type={multiple ? "checkbox" : "radio"}
                      name={question.id}
                      value={option.id}
                      checked={(selected[question.id] ?? []).includes(option.id)}
                      onChange={() => choose(question.id, option.id, multiple)}
                    />
                    <span>{option.label}</span>
                  </label>
                ))}
              </fieldset>
            );
          })}
          <button type="submit" className="min-h-12 rounded-2xl bg-library px-4 text-white" disabled={pending}>إرسال الإجابات</button>
        </form>
      ) : null}
      {result ? (
        <div className="mt-6 space-y-4" role="status" aria-live="assertive">
          <p className="font-display text-3xl">{result.score} — {passingLabel(result.passed)}</p>
          <ul className="space-y-3">
            {result.questions.map((question) => (
              <li key={question.questionId} className="rounded-2xl border border-line p-4">
                <p>{question.accepted ? "إجابة صحيحة" : "إجابة غير صحيحة"}</p>
                <p className="mt-2 leading-8">{question.explanation}</p>
                {question.referenceHref ? <Link href={question.referenceHref} className="mt-2 inline-block text-library">{question.referenceTitle}</Link> : null}
              </li>
            ))}
          </ul>
        </div>
      ) : null}
    </section>
  );
}
