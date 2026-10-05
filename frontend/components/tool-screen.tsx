"use client";

import { useEffect, useId, useRef, useState, type FormEvent } from "react";
import { apiBase } from "@/lib/dictionary";
import {
  type EntryLink,
  type SenseView,
  type ToolDefinition,
  type ToolEnvelope,
  type WordCard,
} from "@/lib/tools";

type Props = {
  tool: ToolDefinition;
  initialQuery?: string;
  initialLeft?: string;
  initialRight?: string;
};

export function ToolScreen({ tool, initialQuery = "", initialLeft = "", initialRight = "" }: Props) {
  const [query, setQuery] = useState(initialQuery);
  const [left, setLeft] = useState(initialLeft);
  const [right, setRight] = useState(initialRight);
  const [envelope, setEnvelope] = useState<ToolEnvelope<Record<string, unknown>> | null>(null);
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);
  const resultRef = useRef<HTMLElement>(null);
  const fieldId = useId();
  const errorId = useId();
  const hintId = useId();
  const describedBy = error ? `${hintId} ${errorId}` : hintId;

  useEffect(() => {
    const ready = tool.fields === "pair" ? initialLeft.trim() && initialRight.trim() : initialQuery.trim();
    if (!ready || tool.fields === "morphology") {
      return;
    }
    let cancelled = false;
    void request(tool, initialQuery, initialLeft, initialRight).then((outcome) => {
      if (cancelled) return;
      setEnvelope(outcome.envelope);
      setError(outcome.error);
      setPending(false);
    });
    return () => {
      cancelled = true;
    };
  }, [tool, initialQuery, initialLeft, initialRight]);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setError("");
    const params = new URLSearchParams();
    if (tool.fields === "pair") {
      params.set("a", left.trim());
      params.set("b", right.trim());
    } else if (query.trim()) {
      params.set("q", query.trim());
    }
    window.history.replaceState(null, "", params.toString() ? `${tool.route}?${params}` : tool.route);
    const outcome = await request(tool, query, left, right);
    setEnvelope(outcome.envelope);
    setError(outcome.error);
    setPending(false);
    resultRef.current?.focus();
  }

  return (
    <main id="content" className="relative z-10 mx-auto w-full max-w-3xl px-5 py-10">
      <p className="text-sm text-library">الأدوات اللغوية</p>
      <h1 className="mt-2 font-display text-5xl sm:text-6xl">{tool.name}</h1>
      <p className="mt-4 leading-8 text-muted">{tool.description}</p>
      <form onSubmit={onSubmit} className="mt-8 space-y-3">
        {tool.fields === "pair" ? (
          <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
            <Field id={`${fieldId}-a`} label="الكلمة الأولى" value={left} placeholder="مثال: كتاب" describedBy={describedBy} onChange={setLeft} />
            <Field id={`${fieldId}-b`} label="الكلمة الثانية" value={right} placeholder="مثال: كاتب" describedBy={describedBy} onChange={setRight} />
          </div>
        ) : (
          <Field id={fieldId} label={tool.inputKind} value={query} placeholder={tool.placeholder} describedBy={describedBy} onChange={setQuery} required={tool.code !== "PATTERNS"} />
        )}
        <p id={hintId} className="text-sm text-muted">
          المدخل: {tool.inputKind}. النتيجة تبقى في صفحة الأداة، والصفحات الأصلية للمرجع تبقى المصدر.
        </p>
        {error ? (
          <p id={errorId} role="alert">
            {error}
          </p>
        ) : null}
        <button type="submit" className="min-h-14 rounded-2xl bg-library px-6 text-white" disabled={pending}>
          {pending ? "جارٍ البحث" : "استخدم الأداة"}
        </button>
      </form>
      <section ref={resultRef} tabIndex={-1} className="mt-8 focus:outline-none" aria-live="polite">
        {envelope ? <ToolResult tool={tool} envelope={envelope} /> : null}
      </section>
    </main>
  );
}

function Field({
  id,
  label,
  value,
  placeholder,
  describedBy,
  onChange,
  required = true,
}: {
  id: string;
  label: string;
  value: string;
  placeholder: string;
  describedBy: string;
  onChange: (value: string) => void;
  required?: boolean;
}) {
  return (
    <div>
      <label htmlFor={id} className="mb-2 block text-sm">
        {label}
      </label>
      <input
        id={id}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        aria-describedby={describedBy}
        className="min-h-14 w-full rounded-2xl border border-line bg-raised px-4 text-lg"
        required={required}
      />
    </div>
  );
}

async function request(tool: ToolDefinition, query: string, left: string, right: string) {
  const params = new URLSearchParams();
  if (tool.fields === "pair") {
    params.set("a", left.trim());
    params.set("b", right.trim());
  } else if (query.trim()) {
    params.set("q", query.trim());
  }
  try {
    const response = await fetch(`${apiBase()}${tool.endpoint}?${params}`, { cache: "no-store" });
    const body = (await response.json()) as { data?: ToolEnvelope<Record<string, unknown>>; message?: string };
    if (!response.ok || !body.data) {
      return { envelope: null, error: body.message || "تعذر تنفيذ الأداة." };
    }
    return { envelope: body.data, error: "" };
  } catch {
    return { envelope: null, error: "تعذر الاتصال بالأداة." };
  }
}

function ToolResult({ tool, envelope }: { tool: ToolDefinition; envelope: ToolEnvelope<Record<string, unknown>> }) {
  const result = envelope.result;
  return (
    <div className="space-y-4">
      <Provenance notes={envelope.provenance} limitations={envelope.limitations} />
      {envelope.status === "EMPTY" ? <p>لا توجد نتيجة منشورة لهذا المدخل.</p> : null}
      {tool.code === "ROOT" ? <RootResult result={result} /> : null}
      {tool.code === "DERIVATIONS" ? <DerivationResult result={result} /> : null}
      {tool.code === "PATTERNS" ? <PatternResult result={result} /> : null}
      {tool.code === "WORD_ANALYSIS" ? <WordResult result={result} /> : null}
      {tool.code === "COMPARE" ? <CompareResult result={result} /> : null}
      {tool.code === "RELATIONS" ? <RelationResult result={result} /> : null}
      {tool.code === "SPELLING_CHECK" ? <SpellingResult result={result} /> : null}
      {tool.code === "GRAMMAR" ? <GrammarResult result={result} /> : null}
      {tool.code === "EXPLORE" ? <GraphResult result={result} /> : null}
    </div>
  );
}

function Provenance({ notes, limitations }: { notes: { label: string }[]; limitations: string[] }) {
  return (
    <div className="rounded-2xl border border-line bg-raised p-4 text-sm leading-7">
      {notes.map((note) => (
        <p key={note.label}>مصدر النتيجة: {note.label}</p>
      ))}
      {limitations.map((item) => (
        <p key={item}>{item}</p>
      ))}
    </div>
  );
}

function RootResult({ result }: { result: Record<string, unknown> }) {
  const entries = asLinks(result.entries);
  const patterns = asLinks(result.patterns);
  return (
    <article className="space-y-3">
      <h2 className="font-display text-3xl">{text(result.heading)}</h2>
      {text(result.root) ? <p className="text-2xl">{text(result.root)}</p> : null}
      {text(result.href) ? <a className="text-library" href={text(result.href)}>صفحة الجذر</a> : null}
      <LinkList title="المداخل المرتبطة" links={entries} />
      <LinkList title="الأوزان المسجّلة" links={patterns} />
    </article>
  );
}

function DerivationResult({ result }: { result: Record<string, unknown> }) {
  const groups = Array.isArray(result.groups) ? result.groups as { partOfSpeechLabel?: string; entries?: EntryLink[] }[] : [];
  return (
    <article className="space-y-4">
      <h2 className="font-display text-3xl">{text(result.root) || "لا يوجد جذر منشور"}</h2>
      {text(result.href) ? <a className="text-library" href={text(result.href)}>صفحة الجذر</a> : null}
      {groups.map((group) => (
        <LinkList key={group.partOfSpeechLabel} title={group.partOfSpeechLabel || "مداخل"} links={group.entries ?? []} />
      ))}
      <LinkList title="تحليلات منشورة" links={asLinks(result.recordedPatterns)} />
    </article>
  );
}

function PatternResult({ result }: { result: Record<string, unknown> }) {
  const patterns = Array.isArray(result.patterns) ? result.patterns as Record<string, unknown>[] : [];
  if (patterns.length === 0) return null;
  return (
    <div className="space-y-4">
      {patterns.map((pattern) => (
        <article key={text(pattern.code)} className="rounded-[1.5rem] border border-line bg-raised p-5">
          <h2 className="font-display text-3xl">{text(pattern.original)}</h2>
          <p className="mt-2 text-sm text-muted">{text(pattern.categoryLabel)} · الأصول: {String(pattern.radicalCount ?? "")}</p>
          <p className="mt-2 leading-7">{text(pattern.description)}</p>
          <p className="mt-2 text-sm">{text(pattern.coverage)}</p>
          <LinkList title="أمثلة منشورة" links={asLinks(pattern.examples)} />
        </article>
      ))}
    </div>
  );
}

function WordResult({ result }: { result: Record<string, unknown> }) {
  const entries = Array.isArray(result.entries) ? result.entries as WordCard[] : [];
  return (
    <div className="space-y-4">
      {text(result.notice) ? <p className="font-medium">{text(result.notice)}</p> : null}
      {entries.map((card) => (
        <WordCardView key={card.slug} card={card} />
      ))}
    </div>
  );
}

function CompareResult({ result }: { result: Record<string, unknown> }) {
  return (
    <div className="space-y-4">
      <p>{text(result.semanticDifference)}</p>
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
        <WordCardView card={result.left as WordCard | null} />
        <WordCardView card={result.right as WordCard | null} />
      </div>
    </div>
  );
}

function RelationResult({ result }: { result: Record<string, unknown> }) {
  const senses = Array.isArray(result.senses) ? result.senses as SenseView[] : [];
  return (
    <div className="space-y-4">
      {text(result.notice) ? <p>{text(result.notice)}</p> : null}
      {senses.map((sense, index) => (
        <article key={`${sense.definition}-${index}`} className="rounded-[1.5rem] border border-line bg-raised p-5">
          <h2 className="font-display text-2xl">معنى {index + 1}</h2>
          <p className="mt-2 leading-7">{sense.definition}</p>
          <LinkList title="مرادفات هذا المعنى" links={sense.synonyms} />
          <LinkList title="أضداد هذا المعنى" links={sense.antonyms} />
          <LinkList title="كلمات مرتبطة بهذا المعنى" links={sense.related} />
        </article>
      ))}
    </div>
  );
}

function SpellingResult({ result }: { result: Record<string, unknown> }) {
  const evidence = Array.isArray(result.evidence) ? result.evidence as Record<string, unknown>[] : [];
  const suggestions = Array.isArray(result.suggestions) ? result.suggestions as { title?: string; href?: string }[] : [];
  return (
    <article className="space-y-3">
      <h2 className="font-display text-3xl">{text(result.verdict)}</h2>
      <ul className="space-y-2">
        {evidence.map((item, index) => (
          <li key={`${text(item.href)}-${index}`}>
            {text(item.kindLabel)}
            {text(item.recordedForm) ? `: ${text(item.recordedForm)}` : ""}
            {text(item.href) ? <> — <a className="text-library" href={text(item.href)}>المصدر</a></> : null}
          </li>
        ))}
      </ul>
      {suggestions.length > 0 ? (
        <div>
          <h3 className="font-display text-2xl">{text(result.suggestionsLabel)}</h3>
          <ul>
            {suggestions.map((item) => (
              <li key={item.href}><a className="text-library" href={item.href}>{item.title}</a></li>
            ))}
          </ul>
        </div>
      ) : null}
    </article>
  );
}

function GrammarResult({ result }: { result: Record<string, unknown> }) {
  const items = Array.isArray(result.items) ? result.items as Record<string, unknown>[] : [];
  return (
    <div className="space-y-4">
      {items.map((item) => (
        <article key={text(item.href)} className="rounded-[1.5rem] border border-line bg-raised p-5">
          <p className="text-sm text-muted">{text(item.kindLabel)}</p>
          <h2 className="font-display text-3xl"><a className="text-library" href={text(item.href)}>{text(item.title)}</a></h2>
          <p className="mt-2 leading-7">{text(item.summary)}</p>
        </article>
      ))}
    </div>
  );
}

function GraphResult({ result }: { result: Record<string, unknown> }) {
  const nodes = Array.isArray(result.list) ? result.list as { id: string; label: string; href?: string; kindLabel?: string }[] : [];
  const edges = Array.isArray(result.edges) ? result.edges as { from: string; to: string; relationLabel: string }[] : [];
  return (
    <div className="space-y-4">
      <div className="hidden flex-wrap gap-2 md:flex" aria-hidden="true">
        {nodes.map((node) => (
          <span key={node.id} className="rounded-full border border-line px-3 py-1">{node.label}</span>
        ))}
      </div>
      <ul className="space-y-2">
        {nodes.map((node) => (
          <li key={node.id}>
            {node.kindLabel}: {node.href ? <a className="text-library" href={node.href}>{node.label}</a> : node.label}
          </li>
        ))}
      </ul>
      {edges.length > 0 ? (
        <ul className="space-y-1 text-sm text-muted">
          {edges.map((edge, index) => (
            <li key={`${edge.from}-${edge.to}-${index}`}>{edge.relationLabel}</li>
          ))}
        </ul>
      ) : null}
    </div>
  );
}

function WordCardView({ card }: { card: WordCard | null }) {
  if (!card) {
    return <article className="rounded-[1.5rem] border border-line bg-raised p-5">غير متوفر في المرجع حاليًا</article>;
  }
  return (
    <article className="rounded-[1.5rem] border border-line bg-raised p-5">
      <h2 className="font-display text-3xl"><a className="text-library" href={card.href}>{card.lemma}</a></h2>
      <p className="mt-2 text-sm text-muted">{card.partOfSpeechLabel}</p>
      {card.root ? <p className="mt-2">{card.rootLabel}: {card.rootHref ? <a className="text-library" href={card.rootHref}>{card.root}</a> : card.root}</p> : null}
      {card.senses.map((sense, index) => (
        <p key={`${sense.definition}-${index}`} className="mt-3 leading-7">{sense.definition}</p>
      ))}
      {card.forms.length > 0 ? <p className="mt-3 text-sm">الصيغ: {card.forms.join("، ")}</p> : null}
      {card.morphology.map((line) => (
        <p key={line.pattern} className="mt-2 text-sm">{line.pattern} · {line.categoryLabel} · {line.provenanceLabel}</p>
      ))}
      <LinkList title="النحو" links={card.grammar.map((link) => ({ lemma: link.label, slug: link.href, href: link.href }))} />
      <LinkList title="الإملاء" links={card.spelling.map((link) => ({ lemma: link.label, slug: link.href, href: link.href }))} />
      {card.missing.map((item) => (
        <p key={item} className="mt-2 text-sm">{item}</p>
      ))}
    </article>
  );
}

function LinkList({ title, links }: { title: string; links: EntryLink[] }) {
  if (links.length === 0) return null;
  return (
    <div className="mt-3">
      <h3 className="text-sm text-muted">{title}</h3>
      <ul>
        {links.map((link) => (
          <li key={`${link.href}-${link.lemma}`}>
            <a className="text-library" href={link.href}>{link.lemma}</a>
            {link.partOfSpeechLabel ? <span className="text-sm text-muted"> · {link.partOfSpeechLabel}</span> : null}
          </li>
        ))}
      </ul>
    </div>
  );
}

function asLinks(value: unknown): EntryLink[] {
  return Array.isArray(value) ? value as EntryLink[] : [];
}

function text(value: unknown): string {
  return typeof value === "string" ? value : "";
}
