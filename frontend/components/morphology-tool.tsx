"use client";

import { useEffect, useId, useRef, useState, type FormEvent } from "react";
import { apiBase } from "@/lib/dictionary";
import {
  featureLines,
  morphologyLabel,
  segmentationLine,
  type AnalysisReport,
  type MorphCandidate,
} from "@/lib/morphology";

export function MorphologyTool({ initialWord = "" }: { initialWord?: string }) {
  const [word, setWord] = useState(initialWord);
  const [report, setReport] = useState<AnalysisReport | null>(null);
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);
  const resultRef = useRef<HTMLElement>(null);
  const fieldId = useId();
  const errorId = useId();
  const hintId = useId();

  useEffect(() => {
    if (!initialWord.trim()) return;
    let cancelled = false;
    void analyzeWord(initialWord).then((outcome) => {
      if (cancelled) return;
      setReport(outcome.report);
      setError(outcome.error);
    });
    return () => {
      cancelled = true;
    };
  }, [initialWord]);

  async function analyze(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setError("");
    window.history.replaceState(null, "", `/tools/morphology?word=${encodeURIComponent(word.trim())}`);
    const outcome = await analyzeWord(word);
    setReport(outcome.report);
    setError(outcome.error);
    setPending(false);
    resultRef.current?.focus();
  }

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <p className="text-sm text-library">الأدوات اللغوية</p>
      <h1 className="mt-2 font-display text-5xl sm:text-6xl">المحلل الصرفي</h1>
      <p className="mt-4 leading-8 text-muted">
        تغطية محدودة من محرك الصرف: مطابقة المعجم المنشور، وقواعد قليلة، وتصريف الفعل الصحيح السالم على وزن فَعَلَ عندما تكون حركته موثّقة.
      </p>
      <form onSubmit={analyze} className="mt-8 space-y-3">
        <label htmlFor={fieldId} className="mb-2 block text-sm">
          كلمة
        </label>
        <input
          id={fieldId}
          value={word}
          onChange={(event) => setWord(event.target.value)}
          placeholder="اكتب كلمة عربية لتحليلها صرفيًا"
          aria-describedby={error ? `${hintId} ${errorId}` : hintId}
          className="min-h-14 w-full rounded-2xl border border-line bg-raised px-4 text-lg"
          required
        />
        <p id={hintId} className="text-sm text-muted">
          المدخل: كلمة واحدة. التحليل لا يخترع جذرًا بلا قاعدة أو مدخل منشور.
        </p>
        {error ? (
          <p id={errorId} role="alert">
            {error}
          </p>
        ) : null}
        <button type="submit" className="min-h-14 rounded-2xl bg-library px-6 text-white" disabled={pending}>
          {pending ? "جارٍ التحليل" : "حلّل"}
        </button>
      </form>
      <section ref={resultRef} tabIndex={-1} className="focus:outline-none" aria-live="polite">
        {report ? <Results report={report} /> : null}
      </section>
    </main>
  );
}

async function analyzeWord(word: string) {
  try {
    const response = await fetch(`${apiBase()}/api/v1/public/morphology/analyze?word=${encodeURIComponent(word)}`, { cache: "no-store" });
    const body = (await response.json()) as { data?: AnalysisReport; message?: string };
    if (!response.ok || !body.data) {
      return { report: null, error: body.message || "تعذر تحليل هذه الكلمة." };
    }
    return { report: body.data, error: "" };
  } catch {
    return { report: null, error: "تعذر الاتصال بالمحلل." };
  }
}

function Results({ report }: { report: AnalysisReport }) {
  return (
    <section className="mt-8" aria-labelledby="morphology-results">
      <h2 id="morphology-results" className="font-display text-3xl">
        {report.analyses.length > 1 ? "تحليلات محتملة" : "نتيجة التحليل"}
      </h2>
      <p className="mt-2 text-sm text-muted">إصدار القواعد: {report.ruleSetVersion}</p>
      {report.truncated ? <p className="mt-2">عُرضت التحليلات الأولى فقط، والعدد أكبر من حد العرض.</p> : null}
      {report.analyses.length === 0 ? <p className="mt-4">لا يوجد تحليل مدعوم لهذه الكلمة في التغطية الحالية.</p> : null}
      <div className="mt-4 space-y-4">
        {report.analyses.map((candidate, index) => (
          <AnalysisCard key={`${candidate.provenance}-${candidate.lexicalEntryId ?? "none"}-${index}`} candidate={candidate} />
        ))}
      </div>
    </section>
  );
}

function AnalysisCard({ candidate }: { candidate: MorphCandidate }) {
  const features = featureLines(candidate.features);
  return (
    <article className="rounded-[1.5rem] border border-line bg-raised p-5">
      <p className="text-sm">
        مصدر التحليل: <strong>{morphologyLabel(candidate.provenance)}</strong>
      </p>
      <dl className="mt-4 grid gap-3 sm:grid-cols-2">
        <Fact term="الكلمة" value={candidate.surfaceForm} />
        <Fact term="الصيغة المعجمية" value={candidate.lemma} />
        <Fact term="الجذر" value={candidate.root} />
        <Fact term="الوزن" value={candidate.patternOriginal} />
        <Fact term="النوع" value={morphologyLabel(candidate.patternCategory || candidate.partOfSpeech)} />
        <Fact term="الاشتقاق" value={morphologyLabel(candidate.derivation)} />
        <Fact term="التقسيم" value={segmentationLine(candidate)} />
      </dl>
      {features.length > 0 ? (
        <ul className="mt-4 space-y-1 text-sm">
          {features.map((line) => (
            <li key={line}>{line}</li>
          ))}
        </ul>
      ) : null}
    </article>
  );
}

function Fact({ term, value }: { term: string; value?: string | null }) {
  if (!value) return null;
  return (
    <div>
      <dt className="text-sm text-muted">{term}</dt>
      <dd className="text-lg">{value}</dd>
    </div>
  );
}
