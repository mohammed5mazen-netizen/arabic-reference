import type { ReactNode } from "react";

export function ErrorState({ title, description, action }: { title: string; description: string; action?: ReactNode }) {
  return (
    <div role="alert" className="rounded-[var(--radius)] border border-line bg-raised p-6">
      <h1 className="font-display text-4xl">{title}</h1>
      <p className="mt-3 leading-8 text-muted">{description}</p>
      {action ? <div className="mt-4">{action}</div> : null}
    </div>
  );
}
