import type { ReactNode } from "react";

export function PageHeader({ eyebrow, title, summary, meta, actions }: { eyebrow?: string; title: string; summary?: string; meta?: string; actions?: ReactNode }) {
  return (
    <header className="max-w-3xl">
      {eyebrow ? <p className="type-label text-library">{eyebrow}</p> : null}
      <h1 className="mt-2 font-display text-5xl leading-tight break-words sm:text-6xl">{title}</h1>
      {summary ? <p className="reading mt-4 text-muted">{summary}</p> : null}
      {meta ? <p className="type-meta mt-3">{meta}</p> : null}
      {actions ? <div className="mt-4 flex flex-wrap gap-3">{actions}</div> : null}
    </header>
  );
}
